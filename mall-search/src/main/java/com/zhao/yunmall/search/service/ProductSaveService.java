package com.zhao.yunmall.search.service;

import com.zhao.common.to.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author yuyun zhao
 * @date 2022/1/5 20:23
 */
public interface ProductSaveService {

	boolean productStatusUp(List<SkuEsModel> skuEsModelList) throws IOException;
}
