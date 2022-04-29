package com.cym.config;

import java.io.File;

import javax.sql.DataSource;

import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.sqlhelper.config.DataSourceEmbed;
import com.cym.sqlhelper.utils.ImportOrExportUtil;

import cn.hutool.core.io.FileUtil;
import cn.hutool.db.ds.simple.SimpleDataSource;

@Configuration
public class SqliteToH2 {
	Logger logger = LoggerFactory.getLogger(VersionConfig.class);

	@Inject
	ImportOrExportUtil importOrExportUtil;
	@Inject
	HomeConfig homeConfig;
	@Inject
	DataSourceEmbed dataSourceEmbed;
	@Inject("${spring.database.type}")
	String databaseType;

	DataSource dataSourceTemp;

	@Init
	public void init() {
		logger.info("databaseType:" + databaseType);

		// 检查是否存在sqlite.db, 进行数据备份
		if ((databaseType.equals("h2") || databaseType.equals("sqlite")) && FileUtil.exist(homeConfig.home + "sqlite.db")) {
			logger.info("开始进行数据转移 " + homeConfig.home + "sqlite.db");

			// 创建sqliteDataSource
			dataSourceTemp = dataSourceEmbed.getDataSource();
			DataSource dataSource = new SimpleDataSource("jdbc:sqlite:" + homeConfig.home + "sqlite.db", "", "");
			dataSourceEmbed.setDataSource(dataSource);

			// 导出数据
			importOrExportUtil.exportDb(homeConfig.home + "dateBak.zip");

			// 重新构建dataSource
			dataSourceEmbed.setDataSource(dataSourceTemp);

			// 导入数据库
			importOrExportUtil.importDb(homeConfig.home + "dateBak.zip");
			FileUtil.del(homeConfig.home + "dateBak.zip");

			// 重命名sqlite.db
			FileUtil.rename(new File(homeConfig.home + "sqlite.db"), "sqlite.db.bak", true);
			logger.info("数据转移完成");
		}
	}
}
