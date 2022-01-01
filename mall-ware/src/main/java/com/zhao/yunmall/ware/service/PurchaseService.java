package com.zhao.yunmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.common.utils.PageUtils;
import com.zhao.yunmall.ware.entity.PurchaseEntity;
import com.zhao.yunmall.ware.vo.MergeVo;
import com.zhao.yunmall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-22 13:16:39
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

	PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

	void mergePurchase(MergeVo mergeVo);

	void receive(List<Long> ids);

	void done(PurchaseDoneVo doneVo);
}

