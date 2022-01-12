package com.zhao.yunmall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.zhao.yunmall.thirdparty.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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


	@Test
	void sendSms() {
		String host = "https://cxcdx.market.alicloudapi.com";
		String path = "/cdcxlongsms/dxjk";
		String method = "GET";
		String appcode = "37023c3c23914719a9685a97a492a180";
		Map<String, String> headers = new HashMap<String, String>();
		//最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
		headers.put("Authorization", "APPCODE " + appcode);
		Map<String, String> querys = new HashMap<String, String>();
		querys.put("content", "【云啵来咯】测试测试有木有收到短信！！");
		querys.put("mobile", "15014630785");
		Map<String, String> bodys = new HashMap<String, String>();


		try {
			/**
			 * 重要提示如下:
			 * HttpUtils请从
			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
			 * 下载
			 *
			 * 相应的依赖请参照
			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
			 */
			HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
			//获取response的body
			//System.out.println(EntityUtils.toString(response.getEntity()));
			System.out.println(EntityUtils.toString(response.getEntity()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
