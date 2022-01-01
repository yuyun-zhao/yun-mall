package com.zhao.yunmall.ware.service.impl;

import com.zhao.common.constant.WareConstant;
import com.zhao.yunmall.ware.entity.PurchaseDetailEntity;
import com.zhao.yunmall.ware.service.PurchaseDetailService;
import com.zhao.yunmall.ware.service.WareSkuService;
import com.zhao.yunmall.ware.vo.MergeVo;
import com.zhao.yunmall.ware.vo.PurchaseDoneVo;
import com.zhao.yunmall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;

import com.zhao.yunmall.ware.dao.PurchaseDao;
import com.zhao.yunmall.ware.entity.PurchaseEntity;
import com.zhao.yunmall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

	@Autowired
	PurchaseDetailService detailService;

	@Autowired
	WareSkuService wareSkuService;


	@Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

	@Override
	public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {
		IPage<PurchaseEntity> page = this.page(
				new Query<PurchaseEntity>().getPage(params),
				new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
		);

		return new PageUtils(page);
	}

	/**
	 * 将采购需求组item合并到一个采购单id里，如果没传入采购单id，则新建一个采购单，
	 * 将items需求组里的需求的采购单id都设置为该id
	 * @param mergeVo
	 */
	@Transactional
	@Override
	public void mergePurchase(MergeVo mergeVo) {
		Long purchaseId = mergeVo.getPurchaseId();

		if (purchaseId == null) {
			// 如果前端没传入采购单id，则新建一个采购单
			PurchaseEntity purchaseEntity = new PurchaseEntity();
			purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
			// 保存该采购单到数据库中
			this.save(purchaseEntity);
			// 回传的id设置上去
			purchaseId = purchaseEntity.getId();
		}
		// TODO 确认采购单状态是0,1才可以合并


		List<Long> items = mergeVo.getItems();
		Long finalPurchaseId = purchaseId;
		List<PurchaseDetailEntity> collect = items.stream().map(i -> {
			PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
			// 设置每个需求的采购单id和状态
			detailEntity.setId(i);
			detailEntity.setPurchaseId(finalPurchaseId);
			detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
			return detailEntity;
		}).collect(Collectors.toList());

		// 设置好采购单id后，就可以更新到数据库中了
		detailService.updateBatchById(collect);
	}

	/**
	 * 先改变该采购单的采购状态status为已采购。前提是当前采购单是新建或者已分配状态
	 * @param ids
	 */
	@Override
	public void receive(List<Long> ids) {
		// 1. 先查出所有符合条件的采购单
		List<PurchaseEntity> collect = ids.stream().map(id -> {
			// 先根据传入的采购单id查出对应的采购单实体对象
			PurchaseEntity purchaseEntity = this.getById(id);
			return purchaseEntity;
		}).filter(item -> {
			// 判断每一个查询到的采购单实体对象，其状态是否是新建或者已分配
			if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
					|| item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
				return true;
			} else {
				return false;
			}
		}).map(item -> {
			// 2. 已经查出了符合条件的采购单，此时需要改变采购单的状态
			item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
			return item;
		}).collect(Collectors.toList());

		// 修改了状态后及时保存
		this.updateBatchById(collect);

		// 3、改变采购项的状态
		collect.forEach((item)->{
			List<PurchaseDetailEntity> entities = detailService.listDetailByPurchaseId(item.getId());
			List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {
				PurchaseDetailEntity entity1 = new PurchaseDetailEntity();
				entity1.setId(entity.getId());
				entity1.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
				return entity1;
			}).collect(Collectors.toList());
			detailService.updateBatchById(detailEntities);
		});
	}

	@Transactional
	@Override
	public void done(PurchaseDoneVo doneVo) {

		Long id = doneVo.getId();


		//2、改变采购项的状态
		Boolean flag = true;
		List<PurchaseItemDoneVo> items = doneVo.getItems();

		List<PurchaseDetailEntity> updates = new ArrayList<>();
		for (PurchaseItemDoneVo item : items) {
			PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
			if(item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()){
				flag = false;
				detailEntity.setStatus(item.getStatus());
			}else{
				detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
				////3、将成功采购的进行入库
				PurchaseDetailEntity entity = detailService.getById(item.getItemId());
				wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());

			}
			detailEntity.setId(item.getItemId());
			updates.add(detailEntity);
		}

		detailService.updateBatchById(updates);

		//1、改变采购单状态
		PurchaseEntity purchaseEntity = new PurchaseEntity();
		purchaseEntity.setId(id);
		purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISH.getCode():WareConstant.PurchaseStatusEnum.HASERROR.getCode());
		purchaseEntity.setUpdateTime(new Date());
		this.updateById(purchaseEntity);
	}

}