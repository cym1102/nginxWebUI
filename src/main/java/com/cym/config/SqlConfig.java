package com.cym.config;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.cym.model.Admin;
import com.cym.model.Cert;
import com.cym.model.Http;

import cn.craccd.sqlHelper.utils.CriteriaAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;

@Configuration
@ComponentScan(basePackages = { "cn.craccd" })
public class SqlConfig {
	@Autowired
	SqlHelper sqlHelper;

	@PostConstruct
	public void initAdmin() {
		Long count = sqlHelper.findAllCount(Admin.class);

		if (count == 0) {
			Admin admin = new Admin();
			admin.setName("admin");
			admin.setPass("admin");

			sqlHelper.insert(admin);
		}

		count = sqlHelper.findAllCount(Http.class);
		if (count == 0) {
			List<Http> https = new ArrayList<Http>();
			https.add(new Http("include", "mime.types"));
			https.add(new Http("default_type", "application/octet-stream"));
			https.add(new Http("sendfile", "on"));
			https.add(new Http("keepalive_timeout", "65"));
			https.add(new Http("gzip", "on"));
			https.add(new Http("client_max_body_size", "1024m"));

			sqlHelper.insertAll(https);
		}

	}
}
