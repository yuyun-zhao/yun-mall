package com.zhao.yunmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zhao.common.constant.CartConstant;
import com.zhao.common.exception.NoStockException;
import com.zhao.common.to.SkuHasStockVo;
import com.zhao.common.utils.R;
import com.zhao.common.vo.MemberResponseVo;
import com.zhao.yunmall.order.constant.OrderConstant;
import com.zhao.yunmall.order.entity.OrderItemEntity;
import com.zhao.yunmall.order.enume.OrderStatusEnum;
import com.zhao.yunmall.order.feign.CartFeignService;
import com.zhao.yunmall.order.feign.MemberFeignService;
import com.zhao.yunmall.order.feign.ProductFeignService;
import com.zhao.yunmall.order.feign.WareFeignService;
import com.zhao.yunmall.order.interceptor.LoginInterceptor;
import com.zhao.yunmall.order.service.OrderItemService;
import com.zhao.yunmall.order.service.PaymentInfoService;
import com.zhao.yunmall.order.to.OrderCreateTo;
import com.zhao.yunmall.order.to.SpuInfoTo;
import com.zhao.yunmall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;

import com.zhao.yunmall.order.dao.OrderDao;
import com.zhao.yunmall.order.entity.OrderEntity;
import com.zhao.yunmall.order.service.OrderService;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

	@Autowired
	private CartFeignService cartFeignService;

	@Autowired
	private MemberFeignService memberFeignService;

	@Autowired
	private WareFeignService wareFeignService;

	@Autowired
	private ThreadPoolExecutor executor;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private ProductFeignService productFeignService;

	@Autowired
	private OrderItemService orderItemService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private PaymentInfoService paymentInfoService;

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		IPage<OrderEntity> page = this.page(
				new Query<OrderEntity>().getPage(params),
				new QueryWrapper<OrderEntity>()
		);

		return new PageUtils(page);
	}

	@Override
	public OrderConfirmVo confirmOrder() {
		// 从 ThreadLocal 中查出当前会员用户的信息
		MemberResponseVo memberResponseVo = LoginInterceptor.loginUserThreadLocal.get();
		OrderConfirmVo confirmVo = new OrderConfirmVo();
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

		CompletableFuture<Void> itemAndStockFuture = CompletableFuture.supplyAsync(() -> {
			RequestContextHolder.setRequestAttributes(requestAttributes);
			// 任务1. 查出所有选中购物项
			List<OrderItemVo> checkedItems = cartFeignService.getCheckedItems();
			confirmVo.setItems(checkedItems);
			return checkedItems;
		}, executor).thenAcceptAsync((items) -> {
			// 任务4. 查看购物车中每个购物项的库存信息
			List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
			Map<Long, Boolean> hasStockMap = wareFeignService.getSkuHasStock(skuIds).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
			confirmVo.setStocks(hasStockMap);
		}, executor);

		// 任务2. 查出当前会员的所有收货地址
		CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
			List<MemberAddressVo> addressByUserId = memberFeignService.getAddressByUserId(memberResponseVo.getId());
			confirmVo.setMemberAddressVos(addressByUserId);
		}, executor);

		// 任务3. 积分
		confirmVo.setIntegration(memberResponseVo.getIntegration());

		// 任务5. 总价自动计算



		// 任务6. 防重令牌。随机生成一个 UUID 作为令牌保存到 Reids 中，并且返回给客户端。
		// 其在点击【提交订单】时将带上该令牌：key - value = order:token:userId - uuid
		String token = UUID.randomUUID().toString().replace("-", "");
		redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(),
				token, 30, TimeUnit.MINUTES);
		confirmVo.setOrderToken(token);

		// 等待异步任务完成
		try {
			CompletableFuture.allOf(itemAndStockFuture, addressFuture).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return confirmVo;
	}

	/**
	 * 用户点击【提交订单】后：
	 * 根据前端传来的OrderSubmitVo，先验证令牌是否一致，再验证价格和最新购物车内的价格是否一致，
	 * 最后锁库存，完成下单
	 *
	 * @param submitVo
	 * @return
	 */
	@Override
	public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
		SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
		responseVo.setCode(0);
		// 先从 ThreadLocal 中获取当前登录用户的信息
		MemberResponseVo memberResponseVo = LoginInterceptor.loginUserThreadLocal.get();
		// 获取前端传来的校验令牌
		String orderToken = submitVo.getOrderToken();
		// // 1. 验证令牌。核心是要保证令牌的获取(1.1)、判断(1.2)与删除(1.3)必须是原子性的
		// // 1.1 去 Redis 中查找当前用户的令牌
		// String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId());
		// // 1.2 判断客户端的令牌和服务端的令牌是否相等
		// if (orderToken != null && orderToken.equals(redisToken)) {
		// 	// 1.3 令牌验证通过。从 Redis 中删除令牌
		// 	redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId());
		// } else {
		// 	// 令牌不通过
		// }

		// 1. 原子验证令牌并删除令牌
		String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
		Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
				Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()),
				orderToken);
		// 返回 0：令牌验证失败
		// 返回 1：令牌验证成功（成功删除才返回1，否则都返回0）
		if (result == 0L) {
			// 验证失败
			responseVo.setCode(1);
			return responseVo;
		}

		// 2. 创建订单、订单项
		OrderCreateTo order = createOrder(memberResponseVo, submitVo);

		// 3. 验证价格
		BigDecimal payAmount = order.getOrder().getPayAmount();
		BigDecimal payPrice = submitVo.getPayPrice();
		// 如果后台计算出的价格和前台传来的价格不一致（相差0.01）
		if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
			// 价格一致
			// 4. 保存订单
			saveOrder(order);
			// 5. 库存锁定
			// 先提取出需要锁定的订单项
			List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map((item) -> {
				OrderItemVo orderItemVo = new OrderItemVo();
				orderItemVo.setSkuId(item.getSkuId());
				orderItemVo.setCount(item.getSkuQuantity());
				return orderItemVo;
			}).collect(Collectors.toList());
			// 封装出Vo对象：订单号和订单项Vo
			WareSkuLockVo lockVo = new WareSkuLockVo();
			lockVo.setOrderSn(order.getOrder().getOrderSn());
			lockVo.setLocks(orderItemVos);
			// 远程调用库存服务锁定这些订单项的库存
			R r = wareFeignService.orderLockStock(lockVo);
			// 5.1 锁定库存成功
			if (r.getCode() == 0) {
				responseVo.setOrder(order.getOrder());
				responseVo.setCode(0);

				// 发送消息到订单延迟队列，判断过期订单
				// rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrder());
				// 清除购物车记录
				BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(CartConstant.CART_PREFIX + memberResponseVo.getId());
				for (OrderItemEntity orderItem : order.getOrderItems()) {
					ops.delete(orderItem.getSkuId().toString());
				}
				return responseVo;
			} else {
				//5.1 锁定库存失败
				String msg = (String) r.get("msg");
				throw new NoStockException(msg);
			}
		} else {
			responseVo.setCode(2);
		}

		return responseVo;
	}

	private OrderCreateTo createOrder(MemberResponseVo memberResponseVo, OrderSubmitVo submitVo) {
		// 1. 用 IdWorker 生成订单号
		String orderSn = IdWorker.getTimeId();
		// 2. 构建订单 OrderEntity
		OrderEntity entity = buildOrder(memberResponseVo, submitVo, orderSn);
		// 3. 构建订单项 OrderItemEntity
		List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
		// 4. 计算价格
		compute(entity, orderItemEntities);
		// 5. 将订单和订单项信息封装到 OrderCreateTo 对象中
		OrderCreateTo createTo = new OrderCreateTo();
		createTo.setOrder(entity);
		createTo.setOrderItems(orderItemEntities);
		return createTo;
	}

	/**
	 * 构建订单信息 OrderEntity
	 *
	 * @param memberResponseVo
	 * @param submitVo
	 * @param orderSn
	 * @return
	 */
	private OrderEntity buildOrder(MemberResponseVo memberResponseVo, OrderSubmitVo submitVo, String orderSn) {
		OrderEntity orderEntity = new OrderEntity();
		// 1. 设置订单号
		orderEntity.setOrderSn(orderSn);

		// 2. 设置用户信息
		orderEntity.setMemberId(memberResponseVo.getId());
		orderEntity.setMemberUsername(memberResponseVo.getUsername());

		// 3. 获取邮费和收件人信息并设置到订单中
		FareVo fareVo = wareFeignService.getFare(submitVo.getAddrId());
		BigDecimal fare = fareVo.getFare();
		orderEntity.setFreightAmount(fare);
		MemberAddressVo address = fareVo.getAddress();
		orderEntity.setReceiverName(address.getName());
		orderEntity.setReceiverPhone(address.getPhone());
		orderEntity.setReceiverPostCode(address.getPostCode());
		orderEntity.setReceiverProvince(address.getProvince());
		orderEntity.setReceiverCity(address.getCity());
		orderEntity.setReceiverRegion(address.getRegion());
		orderEntity.setReceiverDetailAddress(address.getDetailAddress());

		// 4. 设置订单相关的状态信息
		orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
		orderEntity.setConfirmStatus(0);
		orderEntity.setAutoConfirmDay(7);

		return orderEntity;
	}

	/**
	 * 构建所有订单项数据
	 *
	 * @param orderSn
	 * @return
	 */
	private List<OrderItemEntity> buildOrderItems(String orderSn) {
		// 远程调用购物车服务查询当前勾选的所有购物项信息，将其转换成订单项OrderItemVo
		List<OrderItemVo> checkedItems = cartFeignService.getCheckedItems();
		// 根据每一个OrderItemVo构建出对应的OrderItemEntity（数据库中订单项表对应的实体类）
		List<OrderItemEntity> orderItemEntities = checkedItems.stream().map((item) -> {
			// 根据该购物项信息构建出对应的订单项
			OrderItemEntity orderItemEntity = buildOrderItem(item);
			// 设置订单号
			orderItemEntity.setOrderSn(orderSn);
			return orderItemEntity;
		}).collect(Collectors.toList());
		return orderItemEntities;
	}

	/**
	 * 根据购物车服务传来的OrderItemVo对象构建出对应的OrderItemEntity对象
	 *
	 * @param item
	 * @return
	 */
	private OrderItemEntity buildOrderItem(OrderItemVo item) {
		OrderItemEntity orderItemEntity = new OrderItemEntity();
		Long skuId = item.getSkuId();
		// 1. 设置当前订单项的sku相关属性。该信息在购物项里保存，由远程调用购物车服务得到
		orderItemEntity.setSkuId(skuId);
		orderItemEntity.setSkuName(item.getTitle());
		orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttrValues(), ";"));
		orderItemEntity.setSkuPic(item.getImage());
		orderItemEntity.setSkuPrice(item.getPrice());
		orderItemEntity.setSkuQuantity(item.getCount());

		// 2. 远程调用商品服务，通过skuId查询spu相关属性并设置到订单项中
		R r = productFeignService.getSpuBySkuId(skuId);
		if (r.getCode() == 0) {
			// 解析R对象中的“spuInfo”转换成SpuInfoTo对象
			SpuInfoTo spuInfo = JSON.parseObject(JSON.toJSONString(r.get("spuInfo")), SpuInfoTo.class);
			// 设置spu信息到订单项中
			orderItemEntity.setSpuId(spuInfo.getId());
			orderItemEntity.setSpuName(spuInfo.getSpuName());
			orderItemEntity.setSpuBrand(spuInfo.getBrandName());
			orderItemEntity.setCategoryId(spuInfo.getCatalogId());
		}
		// 3. 商品的优惠信息(不做)

		// 4. 商品的积分成长，为价格x数量
		orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
		orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());

		// 5. 订单项订单价格信息
		orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
		orderItemEntity.setCouponAmount(BigDecimal.ZERO);
		orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);

		// 6. 实际价格
		BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
		BigDecimal realPrice = origin.subtract(orderItemEntity.getPromotionAmount())
				.subtract(orderItemEntity.getCouponAmount())
				.subtract(orderItemEntity.getIntegrationAmount());
		orderItemEntity.setRealAmount(realPrice);

		return orderItemEntity;
	}

	private void compute(OrderEntity entity, List<OrderItemEntity> orderItemEntities) {
		// 订单总价
		BigDecimal total = BigDecimal.ZERO;
		// 订单优惠价格
		BigDecimal promotion = new BigDecimal("0.0");
		BigDecimal integration = new BigDecimal("0.0");
		BigDecimal coupon = new BigDecimal("0.0");
		// 积分
		Integer integrationTotal = 0;
		Integer growthTotal = 0;

		// 设置每一个订单项的各种价格信息
		for (OrderItemEntity orderItemEntity : orderItemEntities) {
			total = total.add(orderItemEntity.getRealAmount());
			promotion = promotion.add(orderItemEntity.getPromotionAmount());
			integration = integration.add(orderItemEntity.getIntegrationAmount());
			coupon = coupon.add(orderItemEntity.getCouponAmount());
			integrationTotal += orderItemEntity.getGiftIntegration();
			growthTotal += orderItemEntity.getGiftGrowth();
		}

		entity.setTotalAmount(total);
		entity.setPromotionAmount(promotion);
		entity.setIntegrationAmount(integration);
		entity.setCouponAmount(coupon);
		entity.setIntegration(integrationTotal);
		entity.setGrowth(growthTotal);

		// 付款价格=商品价格+运费
		entity.setPayAmount(entity.getFreightAmount().add(total));

		// 设置删除状态(0-未删除，1-已删除)
		entity.setDeleteStatus(0);
	}

	private void saveOrder(OrderCreateTo orderCreateTo) {
		OrderEntity order = orderCreateTo.getOrder();
		order.setCreateTime(new Date());
		order.setModifyTime(new Date());
		this.save(order);
		orderItemService.saveBatch(orderCreateTo.getOrderItems());
	}
}