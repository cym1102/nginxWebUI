package com.cym.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import cn.craccd.sqlHelper.utils.SqlHelper;

@Configuration
@ComponentScan(basePackages = { "cn.craccd" })
public class SqlConfig  {
	@Autowired
	SqlHelper sqlHelper;


}
