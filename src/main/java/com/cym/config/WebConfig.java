package com.cym.config;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Resource
	private AdminInterceptor adminInterceptor;
	@Resource
	private FrontInterceptor frontInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 自定义拦截器，添加拦截路径和排除拦截路径
		registry.addInterceptor(adminInterceptor)//
				.addPathPatterns("/adminPage/**") //
				.excludePathPatterns("/lib/**") //
				.excludePathPatterns("/js/**")//
				.excludePathPatterns("/img/**")//
				.excludePathPatterns("/css/**");

		registry.addInterceptor(frontInterceptor)//
				.addPathPatterns("/**")//
				.excludePathPatterns("/lib/**") //
				.excludePathPatterns("/js/**")//
				.excludePathPatterns("/img/**")//
				.excludePathPatterns("/css/**");
	}

}
