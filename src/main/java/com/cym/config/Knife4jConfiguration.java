package com.cym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Knife4j 配置
 * 
 * @author Cym
 *
 */
@Configuration
@EnableKnife4j
public class Knife4jConfiguration {

	@Bean(value = "api")
	public Docket api() {
		return buildDocket("外部调用接口", "com.cym.controller.api");
	}

	/**
	 * 
	 * @param groupName   分组名
	 * @param basePackage 扫描包路径
	 * @return
	 */
	private Docket buildDocket(String groupName, String basePackage) {
		Docket docket = new Docket(DocumentationType.SWAGGER_2)//
				.apiInfo(new ApiInfoBuilder()//
						.title("nginxWebUI")//
						.description("RESTful APIs")//
						.version("1.0")//
						.build())//
				.groupName(groupName) //
				.select()//
				.apis(RequestHandlerSelectors.basePackage(basePackage))//
				.paths(PathSelectors.any())//
				.build();

		return docket;
	}
}