package com.cym.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.cym.model.Http;

import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.io.FileUtil;

@Configuration
@ComponentScan(basePackages = { "cn.craccd" })
public class SqlConfig  {
	@Autowired
	SqlHelper sqlHelper;

	@PostConstruct
	public void initAdmin() {

		Long count = sqlHelper.findAllCount(Http.class);
		if (count == 0) {
			List<Http> https = new ArrayList<Http>();
			https.add(new Http("include", "mime.types"));
			https.add(new Http("default_type", "application/octet-stream"));

			sqlHelper.insertAll(https);
		}

	}

}
