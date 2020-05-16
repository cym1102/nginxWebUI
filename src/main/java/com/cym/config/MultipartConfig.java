package com.cym.config;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartConfig {

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();

		// 置文件大小限制 ,超出此大小页面会抛出异常信息
		factory.setMaxFileSize(DataSize.ofGigabytes(5)); // KB,MB
		// 设置总上传数据总大小
		factory.setMaxRequestSize(DataSize.ofGigabytes(5));
		// 设置文件临时文件夹路径
		factory.setLocation(System.getProperty("java.io.tmpdir"));
		// 如果文件大于这个值，将以文件的形式存储，如果小/于这个值文件将存储在内存中，默认为0
		// factory.setMaxRequestSize(0);
		return factory.createMultipartConfig();
	}
}