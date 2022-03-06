package com.zhao.yunmall.product;

import com.zhao.yunmall.product.entity.BrandEntity;
import com.zhao.yunmall.product.service.BrandService;
import com.zhao.yunmall.product.service.CategoryService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@Data
@RunWith(SpringRunner.class)
@SpringBootTest
public class MallProductApplicationTests {

	@Autowired
	BrandService brandService;

	@Autowired
	CategoryService categoryService;

	@Autowired
	StringRedisTemplate redisTemplate;

	@Test
	public void testFindPath() {
		Long[] catelogPath = categoryService.findCatelogPath(225L);
		log.info("完整路径：{}", Arrays.asList(catelogPath));

	}



	@Test
	public void contextLoads() {
		BrandEntity brandEntity = new BrandEntity();
		brandEntity.setDescript("快手");

		brandService.save(brandEntity);
	}


	@Test
	public void testRedis() {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		ops.set("hello", "world_" + UUID.randomUUID().toString());

		System.out.println(ops.get("hello"));


	}




}
