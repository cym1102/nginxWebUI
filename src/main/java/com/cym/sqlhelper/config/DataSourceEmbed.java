package com.cym.sqlhelper.config;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

import com.cym.config.HomeConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.hutool.db.Db;
import cn.hutool.db.GlobalDbConfig;
import cn.hutool.db.ds.pooled.DbConfig;
import cn.hutool.log.GlobalLogFactory;
import cn.hutool.log.level.Level;

@Component
public class DataSourceEmbed {
	@Inject
	HomeConfig homeConfig;

	@Inject("${spring.database.type}")
	String databaseType;
	@Inject("${spring.datasource.url}")
	String url;
	@Inject("${spring.datasource.username}")
	String username;
	@Inject("${spring.datasource.password}")
	String password;

	HikariDataSource dataSource;

	@Init
	public void init() {
		// 创建dataSource
		if (databaseType.equalsIgnoreCase("sqlite") || databaseType.equalsIgnoreCase("h2")) {

			// 建立新的sqlite数据源
			HikariConfig dbConfig = new HikariConfig();
			dbConfig.setJdbcUrl("jdbc:sqlite:" + homeConfig.home + "sqlite.db");
			dbConfig.setUsername("");
			dbConfig.setPassword("");
			dbConfig.setMaximumPoolSize(1);
			dbConfig.setDriverClassName("org.sqlite.JDBC");
			dataSource = new HikariDataSource(dbConfig);

		} else if (databaseType.equalsIgnoreCase("mysql")) {
			HikariConfig dbConfig = new HikariConfig();
			dbConfig.setJdbcUrl(url);
			dbConfig.setUsername(username);
			dbConfig.setPassword(password);
			dbConfig.setMaximumPoolSize(1);
			dbConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
			dataSource = new HikariDataSource(dbConfig);
		}
		
		
	}

	public HikariDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(HikariDataSource dataSource) {
		this.dataSource = dataSource;
	}

}
