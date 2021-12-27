package com.zhao.yunmall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class MallThirdPartyApplicationTests {
	@Autowired
	OSSClient ossClient;

	@Test
	void contextLoads() throws FileNotFoundException {
		InputStream inputStream = new FileInputStream("C:\\Users\\yuyun zhao\\Desktop\\校历.png");
		// 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
		ossClient.putObject("yunmall-project", "test2.png", inputStream);

		// 关闭OSSClient。
		ossClient.shutdown();
		System.out.println("上传完成...");
	}

}
