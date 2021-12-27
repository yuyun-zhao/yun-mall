package com.zhao.yunmall.product;

import com.zhao.yunmall.product.entity.BrandEntity;
import com.zhao.yunmall.product.service.BrandService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Data
@RunWith(SpringRunner.class)
@SpringBootTest
public class MallProductApplicationTests {

	@Autowired
	BrandService brandService;

	@Test
	public void contextLoads() {
		BrandEntity brandEntity = new BrandEntity();
		brandEntity.setDescript("快手");

		brandService.save(brandEntity);
	}


}
