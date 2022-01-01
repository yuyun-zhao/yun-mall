package com.zhao.yunmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.common.utils.PageUtils;
import com.zhao.yunmall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-22 13:16:39
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

	List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);
}

