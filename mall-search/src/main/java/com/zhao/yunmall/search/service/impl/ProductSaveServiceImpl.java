package com.zhao.yunmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhao.common.to.to.SkuEsModel;
import com.zhao.yunmall.search.config.MallElasticSearchConfig;
import com.zhao.yunmall.search.constant.EsConstant;
import com.zhao.yunmall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuyun zhao
 * @date 2022/1/5 20:25
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {
	@Autowired
	RestHighLevelClient restHighLevelClient;

	/**
	 * 上架sku数据，保存到es中
	 * @param skuEsModelList
	 */
	@Override
	public boolean productStatusUp(List<SkuEsModel> skuEsModelList) throws IOException {
		// 1. 先创建索引: product，并建立好映射关系
		// 事先创建好索引，包括每个字段的类型

		// 2. 在ES中保存这些数据
		BulkRequest bulkRequest = new BulkRequest();
		for (SkuEsModel skuEsModel : skuEsModelList) {
			//构造保存请求
			IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
			indexRequest.id(skuEsModel.getSkuId().toString());
			String jsonString = JSON.toJSONString(skuEsModel);
			indexRequest.source(jsonString, XContentType.JSON);
			bulkRequest.add(indexRequest);
		}
		// 3. 批量保存
		BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, MallElasticSearchConfig.COMMON_OPTIONS);

		//TODO 如果批量错误
		boolean hasFailures = bulk.hasFailures();
		List<String> collect = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
		log.info("商品上架完成：{}", collect);

		return hasFailures;
	}
}
