package com.cym.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.cym.NginxWebUI;

@Component
public class PropertiesUtils {
	static Logger logger = LoggerFactory.getLogger(NginxWebUI.class);
	public Properties getPropertis(String name) {
		Properties properties = new Properties();
		try {
			// 使用ClassLoader加载properties配置文件生成对应的输入流
			ClassPathResource resource = new ClassPathResource(name);
			InputStream in = resource.getInputStream();

			// 使用properties对象加载输入流
			properties.load(in);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		
		return properties;
	}

}
