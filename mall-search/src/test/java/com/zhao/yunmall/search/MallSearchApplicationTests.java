package com.zhao.yunmall.search;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zhao.yunmall.search.config.MallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class MallSearchApplicationTests {
	@Autowired
	private RestHighLevelClient client;

	@Test
	public void contextLoads() {
		System.out.println(client);
	}

	/**
	 * 测试存储数据到ES中
	 */
	@Test
	public void indexDataTest() throws IOException {
		IndexRequest indexRequest = new IndexRequest("users");
		indexRequest.id("1");
		// 方式一：
		// indexRequest.source("userName", "zhangsan", "age", "男");

		User user = new User();
		user.setAge(19);
		user.setGender("男");
		user.setUserName("张三");
		String jsonString = JSON.toJSONString(user);
		indexRequest.source(jsonString, XContentType.JSON);

		// 执行操作
		IndexResponse index = client.index(indexRequest, MallElasticSearchConfig.COMMON_OPTIONS);

		// 提取有用的响应数据
		System.out.println(index);
	}

	@Data
	class User {
		public Integer age;
		public String gender;
		public String userName;
	}

	@Test
	public void searchData() throws IOException {
		SearchRequest searchRequest = new SearchRequest();
		// 1. 指定索引
		searchRequest.indices("bank");
		// 2. 指定DSL，检索条件
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		// 2.1 条件查询
		sourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
		// 2.2 按照年龄值分布进行聚合
		TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
		sourceBuilder.aggregation(ageAgg);
		// 2.3 按照平均薪资进行聚合
		AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
		sourceBuilder.aggregation(balanceAvg);
		// 为查询请求设置建造器
		searchRequest.source(sourceBuilder);

		System.out.println("检索条件：" + sourceBuilder.toString());

		// 3. 执行检索
		SearchResponse searchResponse = client.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);
		System.out.println(searchResponse.toString());

		// 4. 拿到命中得结果
		SearchHits hits = searchResponse.getHits();
		// 5.搜索请求的匹配
		SearchHit[] searchHits = hits.getHits();
		// 6. 进行遍历
		for (SearchHit hit : searchHits) {
			// 7. 拿到完整结果字符串
			String sourceAsString = hit.getSourceAsString();
			// 8. 转换成实体类
			Account accout = JSON.parseObject(sourceAsString, Account.class);
			System.out.println("account:" + accout );
		}

		// 9. 拿到聚合
		Aggregations aggregations = searchResponse.getAggregations();

		// 10. 通过先前名字拿到对应聚合
		Terms ageAgg1 = aggregations.get("ageAgg");
		for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
			// 11、拿到结果
			String keyAsString = bucket.getKeyAsString();
			System.out.println("年龄:" + keyAsString);
			long docCount = bucket.getDocCount();
			System.out.println("个数：" + docCount);
		}
		Avg balanceAvg1 = aggregations.get("balanceAvg");
		System.out.println("平均薪资：" + balanceAvg1.getValue());
		System.out.println(searchResponse.toString());
	}


	@Data
	static class Account {
		@JsonProperty("account_number")
		private Integer accountNumber;
		@JsonProperty("balance")
		private Integer balance;
		@JsonProperty("firstname")
		private String firstname;
		@JsonProperty("lastname")
		private String lastname;
		@JsonProperty("age")
		private Integer age;
		@JsonProperty("gender")
		private String gender;
		@JsonProperty("address")
		private String address;
		@JsonProperty("employer")
		private String employer;
		@JsonProperty("email")
		private String email;
		@JsonProperty("city")
		private String city;
		@JsonProperty("state")
		private String state;
	}

}
