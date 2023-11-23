package com.cym.sqlhelper.config;

import javax.sql.DataSource;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;

import com.cym.config.HomeConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class DataSourceEmbed  {
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

	DataSource dataSource;

	@Init
	public void afterInjection()  {
		// 创建dataSource
		if (databaseType.equalsIgnoreCase("sqlite") || databaseType.equalsIgnoreCase("h2")) {
			HikariConfig dbConfig = new HikariConfig();
			dbConfig.setJdbcUrl("jdbc:h2:" + homeConfig.home + "h2");
			dbConfig.setUsername("sa");
			dbConfig.setPassword("");
			dbConfig.setMaximumPoolSize(1);
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

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
