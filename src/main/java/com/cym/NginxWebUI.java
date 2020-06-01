package com.cym;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import cn.hutool.core.io.FileUtil;

@EnableTransactionManagement
@SpringBootApplication
public class NginxWebUI {

	public static void main(String[] args) {
		String sqliteOrgPath = FileUtil.getUserHomePath() + File.separator + ".sqlite.db";
		if (FileUtil.exist(sqliteOrgPath)) {
			FileUtil.move(new File(sqliteOrgPath), new File("/home/nginxWebUI/sqlite.db "), true);
		}
		
		SpringApplication.run(NginxWebUI.class, args);
	}
}
