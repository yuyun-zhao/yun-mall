package com.zhao.yunmall.ware.service.impl;

import com.zhao.common.exception.NoStockException;
import com.zhao.common.utils.R;
import com.zhao.yunmall.ware.entity.WareOrderTaskDetailEntity;
import com.zhao.yunmall.ware.entity.WareOrderTaskEntity;
import com.zhao.yunmall.ware.feign.OrderFeignService;
import com.zhao.yunmall.ware.feign.ProductFeignService;
import com.zhao.yunmall.ware.service.WareOrderTaskDetailService;
import com.zhao.yunmall.ware.service.WareOrderTaskService;
import com.zhao.yunmall.ware.vo.OrderItemVo;
import com.zhao.yunmall.ware.vo.SkuHasStockVo;
import com.zhao.yunmall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
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

	// @Autowired
	// private RabbitTemplate rabbitTemplate;


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
	 * @param lockVo
	 * @return
	 */
	@Override
	public Boolean orderLockStock(WareSkuLockVo wareSkuLockVo) {
		//因为可能出现订单回滚后，库存锁定不回滚的情况，但订单已经回滚，得不到库存锁定信息，因此要有库存工作单

		WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
		taskEntity.setOrderSn(wareSkuLockVo.getOrderSn());
		taskEntity.setCreateTime(new Date());
		wareOrderTaskService.save(taskEntity);
		// 获取订单项Vo
		List<OrderItemVo> itemVos = wareSkuLockVo.getLocks();
		// 1. 遍历所有订单项，为其创建SkuLockVo：当前SKU在哪些仓库（wareIds）里有库存，并且库存数量是多少（num）
		List<SkuLockVo> lockVos = itemVos.stream().map((item) -> {
			SkuLockVo skuLockVo = new SkuLockVo();
			skuLockVo.setSkuId(item.getSkuId());
			skuLockVo.setNum(item.getCount());
			// 找出所有库存大于商品数的仓库
			List<Long> wareIds = baseMapper.listWareIdsHasStock(item.getSkuId(), item.getCount());
			skuLockVo.setWareIds(wareIds);
			return skuLockVo;
		}).collect(Collectors.toList());

		// 2. 锁定库存。从查询出的还有库存的仓库中找出任意一个仓库，在该仓库中锁定商品
		for (SkuLockVo lockVo : lockVos) {
			boolean lock = true;
			Long skuId = lockVo.getSkuId();
			List<Long> wareIds = lockVo.getWareIds();
			// 如果没有满足条件的仓库，抛出异常
			if (wareIds == null || wareIds.size() == 0) {
				throw new NoStockException(skuId);
			} else {
				// 遍历每一个仓库，看看哪个能锁库存成功
				for (Long wareId : wareIds) {
					// 尝试锁定库存
					Long count = baseMapper.lockWareSku(skuId, lockVo.getNum(), wareId);
					if (count == 0) {
						// 如果锁失败
						lock = false;
					} else {
						// // 如果锁定成功，保存工作单详情
						// WareOrderTaskDetailEntity detailEntity = WareOrderTaskDetailEntity.builder()
						// 		.skuId(skuId)
						// 		.skuName("")
						// 		.skuNum(lockVo.getNum())
						// 		.taskId(taskEntity.getId())
						// 		.wareId(wareId)
						// 		.lockStatus(1).build();
						// wareOrderTaskDetailService.save(detailEntity);
						// //发送库存锁定消息至延迟队列
						// StockLockedTo lockedTo = new StockLockedTo();
						// lockedTo.setId(taskEntity.getId());
						// StockDetailTo detailTo = new StockDetailTo();
						// BeanUtils.copyProperties(detailEntity, detailTo);
						// lockedTo.setDetailTo(detailTo);
						// rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);

						lock = true;
						// 当前订单项锁库存成功，不再尝试其他的仓库了，break
						break;
					}
				}
			}
			// 如果所有仓库都没锁住（都没库存了）
			if (!lock) throw new NoStockException(skuId);
		}
		// 能走到这里，所有都锁定成功了
		return true;
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