package com.cym.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import freemarker.template.TemplateException;

@Configuration
public class FreeMarkerConfig {

	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer() throws IOException, TemplateException {
		FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
		freeMarkerConfigurer.setTemplateLoaderPath("classpath:templates/");

		freemarker.template.Configuration configuration = freeMarkerConfigurer.createConfiguration();
		configuration.setDefaultEncoding("UTF-8");
		configuration.setSetting("classic_compatible", "true");// 使用经典语法
		configuration.setSetting("number_format", "0.##");

		freeMarkerConfigurer.setConfiguration(configuration);
		return freeMarkerConfigurer;
	}

	@Bean
	public FreeMarkerViewResolver freeMarkerViewResolver() {
		FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
		resolver.setPrefix("");
		resolver.setSuffix(".html");
		resolver.setContentType("text/html; charset=UTF-8");
		resolver.setRequestContextAttribute("request"); // 将上下文路径注入request变量

		return resolver;

	}
}
