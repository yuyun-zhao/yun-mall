package com.zhao.yunmall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zhao.common.exception.NoStockException;
import com.zhao.common.to.mq.OrderTo;
import com.zhao.common.to.mq.StockDetailTo;
import com.zhao.common.to.mq.StockLockedTo;
import com.zhao.common.utils.R;
import com.zhao.yunmall.ware.entity.WareOrderTaskDetailEntity;
import com.zhao.yunmall.ware.entity.WareOrderTaskEntity;
import com.zhao.yunmall.ware.enume.OrderStatusEnum;
import com.zhao.yunmall.ware.enume.WareTaskStatusEnum;
import com.zhao.yunmall.ware.feign.OrderFeignService;
import com.zhao.yunmall.ware.feign.ProductFeignService;
import com.zhao.yunmall.ware.service.WareOrderTaskDetailService;
import com.zhao.yunmall.ware.service.WareOrderTaskService;
import com.zhao.yunmall.ware.vo.OrderItemVo;
import com.zhao.yunmall.ware.vo.SkuHasStockVo;
import com.zhao.yunmall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;

import com.zhao.yunmall.ware.dao.WareSkuDao;
import com.zhao.yunmall.ware.entity.WareSkuEntity;
import com.zhao.yunmall.ware.service.WareSkuService;


@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

	@Autowired
	WareSkuDao wareSkuDao;

	@Autowired
	ProductFeignService productFeignService;

	@Autowired
	private WareOrderTaskService wareOrderTaskService;

	@Autowired
	private WareOrderTaskDetailService wareOrderTaskDetailService;

	@Autowired
	private OrderFeignService orderFeignService;

	@Autowired
	private RabbitTemplate rabbitTemplate;


	/**
	 * 在原先信息的基础上，添加两个查询条件：skuId，wareId
	 *
	 * @param params
	 * @return
	 */
	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
		String skuId = (String) params.get("skuId");
		if (!StringUtils.isEmpty(skuId)) {
			queryWrapper.eq("sku_id", skuId);
		}
		String wareId = (String) params.get("wareId");
		if (!StringUtils.isEmpty(wareId)) {
			queryWrapper.eq("ware_id", wareId);
		}
		IPage<WareSkuEntity> page = this.page(
				new Query<WareSkuEntity>().getPage(params),
				queryWrapper
		);


		return new PageUtils(page);
	}

	@Override
	public void addStock(Long skuId, Long wareId, Integer skuNum) {
		//1、判断如果还没有这个库存记录新增
		List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
		if (entities == null || entities.size() == 0) {
			WareSkuEntity skuEntity = new WareSkuEntity();
			skuEntity.setSkuId(skuId);
			skuEntity.setStock(skuNum);
			skuEntity.setWareId(wareId);
			skuEntity.setStockLocked(0);
			//TODO 远程查询sku的名字，如果失败，整个事务无需回滚
			//1、自己catch异常
			//TODO 还可以用什么办法让异常出现以后不回滚？服务降级和熔断
			try {
				R info = productFeignService.info(skuId);
				Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

				if (info.getCode() == 0) {
					skuEntity.setSkuName((String) data.get("skuName"));
				}
			} catch (Exception e) {

			}

			wareSkuDao.insert(skuEntity);
		} else {
			wareSkuDao.addStock(skuId, wareId, skuNum);
		}

	}

	@Override
	public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
		// 遍历每一个sku，判断其是否还有库存，如果有就设置为true
		return skuIds.stream().map(skuId -> {
			Long count = this.baseMapper.getSkuStock(skuId);
			SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
			skuHasStockVo.setSkuId(skuId);
			skuHasStockVo.setHasStock(count != null && count > 0);
			return skuHasStockVo;
		}).collect(Collectors.toList());
	}

	/**
	 * 下单成功后锁定库存
	 *
	 * @param wareSkuLockVo
	 * @return
	 */
	@Override
	public Boolean orderLockStock(WareSkuLockVo wareSkuLockVo) {
		// 0. 持久化"订单&库存-工作单"：主要保存每个订单的订单号 order_sn 信息，方便库存解锁时回溯找到问题订单
		//   其存在的意义：备份订单库存信息。可能出现订单回滚后，库存已经锁定，不会回滚的情况：
		//               此时订单已经回滚了，无法从订单表中获取到需要解锁的订单信息了，
		//               那么就找不到需要解锁的库存信息了，因此需要保存库存工作单，方便解锁库存时能定位到需要解锁的库存
		WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
		taskEntity.setOrderSn(wareSkuLockVo.getOrderSn());
		taskEntity.setCreateTime(new Date());
		// 持久化到数据库中。以便需要解锁库存时可以追溯到需要解锁的订单号以及其库存信息
		wareOrderTaskService.save(taskEntity);

		// 获取订单服务远程传来的订单项 Vo
		List<OrderItemVo> itemVos = wareSkuLockVo.getLocks();
		// 1. 遍历所有订单项，为其创建"SKU-仓库号"信息 SkuLockVo：
		// 包含当前SKU在哪些仓库（wareIds）里有库存，并且库存数量是多少（num）
		List<SkuLockVo> lockVos = itemVos.stream().map((item) -> {
			SkuLockVo skuLockVo = new SkuLockVo();
			skuLockVo.setSkuId(item.getSkuId());
			skuLockVo.setNum(item.getCount());
			// 找出所有库存大于该订单中商品数 num 的仓库，这些仓库中有任何一个可以锁定成功，该订单的库存就能锁定成功
			List<Long> wareIds = baseMapper.listWareIdsHasStock(item.getSkuId(), item.getCount());
			// 保存候选库存 id
			skuLockVo.setWareIds(wareIds);
			return skuLockVo;
		}).collect(Collectors.toList());

		// 2. 遍历每一个订单项，为其锁定库存：从查询出的还有库存的仓库中找出任意一个仓库，在该仓库中锁定当前商品
		for (SkuLockVo lockVo : lockVos) {
			boolean lock = true;
			Long skuId = lockVo.getSkuId();
			List<Long> wareIds = lockVo.getWareIds();
			// 如果没有满足条件的仓库，抛出异常，其他持久化的库存信息全部回滚
			if (wareIds == null || wareIds.size() == 0) {
				throw new NoStockException(skuId);
			} else {
				// 3. 遍历每一个仓库，看看哪个能锁库存成功
				//   3.1 如果每一个商品都能锁定成功，则所有商品的"商品&库存-工作单详情"信息将发送给库存服务的延迟队列里
				//   3.2 如果某一个商品锁定失败。前面保存的工作单信息就回滚了（因为下面手动抛出异常了）
				//       这样前面放到延迟队列里的消息即使在之后需要解锁记录，但由于在数据库中查不到 id（因为回滚了），所以也不会重复解锁
				for (Long wareId : wareIds) {
					// 4. 在数据库中锁定库存：在 wms_ware_sku 表中给 skuId 商品的 stock_locked 字段增加数量 num，代表锁定该商品的num个库存
					Long count = baseMapper.lockWareSku(skuId, lockVo.getNum(), wareId);
					if (count == 0) {
						// 4.1 如果锁定失败（可能库存不足）
						lock = false;
					} else {
						// 4.2 如果锁定成功，保存"商品&库存-工作单详情"信息 WareOrderTaskDetailEntity
						// 该信息中保存了下单项的 skuId、下单数量、"订单&库存-工作单"id、仓库 id 以及锁定状态（已锁定）
						// 未来解锁库存时，将去数据库中查找该工作单详情数据，并据此来进行库存解锁
						WareOrderTaskDetailEntity detailEntity = WareOrderTaskDetailEntity.builder()
								.skuId(skuId)
								.skuName("")
								.skuNum(lockVo.getNum())
								.taskId(taskEntity.getId())
								.wareId(wareId)
								.lockStatus(1).build();
						// 4.3 将"商品&库存-工作单详情" WareOrderTaskDetailEntity 持久化到数据库中
						wareOrderTaskDetailService.save(detailEntity);

						// 封装 StockLockedTo 对象，其将被保存到消息队列中。用于解锁库存时去数据库中定位到库存信息
						StockLockedTo lockedTo = new StockLockedTo();
						StockDetailTo detailTo = new StockDetailTo();
						BeanUtils.copyProperties(detailEntity, detailTo);
						// 设置工作单 id 以及"商品&库存-工作单详情"信息
						lockedTo.setId(taskEntity.getId());
						lockedTo.setDetailTo(detailTo);

						// 4.4 发送封装后的"商品&库存-工作单详情"数据到库存延迟队列 stock.release.stock.queue 中，
						// 将在四十分钟后过期，被 StockReleaseListener 监听后进行库存解锁
						rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);

						lock = true;
						// 当前订单项锁库存成功，不再尝试其他的仓库了，break
						break;
					}
				}
			}
			// 如果有某一个订单在所有仓库都没能成功锁定库存（都没库存了），则抛出异常
			// 令所有持久化到数据里的库存工作单数据都回滚
			if (!lock) throw new NoStockException(skuId);
		}
		// 能走到这里，所有都锁定成功了
		return true;
	}

	/**
	 * 对库存服务延迟队列中过期的消息进行解锁库存判断
	 * 1、没有这个订单，必须解锁库存
	 * 2、有这个订单，不一定解锁库存
	 *    订单状态：已取消：解锁库存
	 *             已支付：不能解锁库存
	 * @param stockLockedTo
	 */
	@Override
	public void unlock(StockLockedTo stockLockedTo) {
		// 1. 获取库存工作详情单
		StockDetailTo detailTo = stockLockedTo.getDetailTo();
		WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(detailTo.getId());
		// 如果工作单详情不为空，说明该库存锁定成功
		if (detailEntity != null) {
			WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(stockLockedTo.getId());
			// 2. 远程调用订单服务查询订单信息
			R r = orderFeignService.infoByOrderSn(taskEntity.getOrderSn());
			if (r.getCode() == 0) {
				OrderTo order = JSON.parseObject(JSON.toJSONString(r.get("orderEntity")), OrderTo.class);
				// 3. 如果没有这个订单 或 订单状态为已取消，则解锁库存
				// 如果该订单存在且状态为新建，或已支付，则不需要解锁库存
				if (order == null || order.getStatus().equals(OrderStatusEnum.CANCLED.getCode())) {
					// 为保证幂等性，只有当工作单详情处于被锁定的情况下才进行解锁
					if (detailEntity.getLockStatus().equals(WareTaskStatusEnum.Locked.getCode())) {
						// 真正执行解锁库存的业务代码
						unlockStock(detailTo.getSkuId(), detailTo.getSkuNum(), detailTo.getWareId(), detailEntity.getId());
					}
				}
			} else {
				// 如果远程调用失败则抛出异常，消费者捕获异常后将消息重新入队等待其他消费者再次消费
				throw new RuntimeException("远程调用订单服务失败");
			}
		}
	}

	/**
	 * 订单服务在关单后，将立即发来消息解锁库存
	 * 作为双重保险，保证库存一定能解锁，防止订单服务因网络延迟而没有将订单状态改为已取消
	 * @param orderTo
	 */
	@Override
	public void unlock(OrderTo orderTo) {
		// 为防止重复解锁，需要重新查询工作单
		String orderSn = orderTo.getOrderSn();
		WareOrderTaskEntity taskEntity = wareOrderTaskService.getBaseMapper()
				.selectOne((new QueryWrapper<WareOrderTaskEntity>()
						.eq("order_sn", orderSn)));
		// 查询出当前订单相关的且处于锁定状态的工作单详情
		List<WareOrderTaskDetailEntity> lockDetails = wareOrderTaskDetailService.list(
				new QueryWrapper<WareOrderTaskDetailEntity>()
						.eq("task_id", taskEntity.getId())
						.eq("lock_status", WareTaskStatusEnum.Locked.getCode()));
		for (WareOrderTaskDetailEntity lockDetail : lockDetails) {
			// 解锁库存
			unlockStock(lockDetail.getSkuId(), lockDetail.getSkuNum(), lockDetail.getWareId(), lockDetail.getId());
		}
	}

	/**
	 * 去数据库中解锁库存并更改库存工作单状态
	 * @param skuId
	 * @param skuNum
	 * @param wareId
	 * @param detailId
	 */
	private void unlockStock(Long skuId, Integer skuNum, Long wareId, Long detailId) {
		// 从数据库中解锁库存数据（现有的库存 + 商品数量 skuNum）
		baseMapper.unlockStock(skuId, skuNum, wareId);
		// 更新库存工作单详情的状态
		WareOrderTaskDetailEntity detail = WareOrderTaskDetailEntity.builder()
				.id(detailId)
				.lockStatus(2).build();
		wareOrderTaskDetailService.updateById(detail);
	}


	/**
	 * 内部类：当前SKU在哪些仓库（wareIds）里有库存，并且库存数量是多少（num）
	 */
	@Data
	class SkuLockVo {
		private Long skuId;
		private Integer num;
		private List<Long> wareIds;
	}

}