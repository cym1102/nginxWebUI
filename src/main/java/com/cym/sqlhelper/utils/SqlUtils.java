package com.cym.sqlhelper.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.hutool.core.util.StrUtil;

@Component
public class SqlUtils {
	static Logger logger = LoggerFactory.getLogger(SqlUtils.class);

	@Inject("${project.sqlPrint:false}")
	Boolean print;
	@Inject
	JdbcTemplate jdbcTemplate;

	String separator = System.getProperty("line.separator");

	public String formatSql(String sql) {
		if (StrUtil.isEmpty(sql)) {
			return "";
		}

		sql = sql.replace("FROM", separator + "FROM")//
				.replace("WHERE", separator + "WHERE")//
				.replace("ORDER", separator + "ORDER")//
				.replace("LIMIT", separator + "LIMIT")//
				.replace("VALUES", separator + "VALUES");//

		return sql;
	}

	public void checkOrCreateTable(Class<?> clazz) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS `" + StrUtil.toUnderlineCase(clazz.getSimpleName()) + "` (id VARCHAR(32) NOT NULL PRIMARY KEY)";
		logQuery(formatSql(sql));
		jdbcTemplate.execute(formatSql(sql));

	}

	public void logQuery(String sql) {
		logQuery(sql, null);
	}

	public void logQuery(String sql, Object[] params) {
		if (print) {
			try {
				if (params != null) {
					for (Object object : params) {

						if (object instanceof String) {
							object = object.toString().replace("$", "RDS_CHAR_DOLLAR");
							sql = sql.replaceFirst("\\?", "'" + object + "'").replace("RDS_CHAR_DOLLAR", "$");
						} else {
							sql = sql.replaceFirst("\\?", String.valueOf(object));
						}

					}
				}
				logger.info(sql);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void checkOrCreateIndex(Class<?> clazz, String name, boolean unique, List<Map<String, Object>> indexs) throws SQLException {
		checkOrCreateIndex(clazz, new String[] { name }, unique, indexs);
	}

	public void checkOrCreateIndex(Class<?> clazz, String[] colums, boolean unique, List<Map<String, Object>> indexs) throws SQLException {
		List<String> columList = new ArrayList<String>();
		for (String colum : colums) {
			columList.add(StrUtil.toUnderlineCase(colum));
		}
		String name = StrUtil.join("&", columList) + "@" + StrUtil.toUnderlineCase(clazz.getSimpleName());

		Boolean hasIndex = false;
		for (Map<String, Object> map : indexs) {
			if (StrUtil.toUnderlineCase(name).equalsIgnoreCase((String) map.get("name")) || StrUtil.toUnderlineCase(name).equalsIgnoreCase((String) map.get("Key_name"))) {
				hasIndex = true;
			}
		}

		if (!hasIndex) {
			String type = unique ? "UNIQUE INDEX" : "INDEX";
			String length = "";

			columList = new ArrayList<String>();
			for (String colum : colums) {
				columList.add(StrUtil.toUnderlineCase("`" + colum + "`" + length));
			}

			String sql = "CREATE " + type + "  `" + StrUtil.toUnderlineCase(name) + "` ON `" + StrUtil.toUnderlineCase(clazz.getSimpleName()) + "`(" + StrUtil.join(",", columList) + ")";
			logQuery(formatSql(sql));
			jdbcTemplate.execute(formatSql(sql));
		}

	}

	public void checkOrCreateColumn(Class<?> clazz, String name, Set<String> columns) throws SQLException {
		if (!columns.contains(StrUtil.toUnderlineCase(name).toLowerCase())) {
			String sql = "ALTER TABLE `" + StrUtil.toUnderlineCase(clazz.getSimpleName()) + "` ADD COLUMN `" + StrUtil.toUnderlineCase(name) + "` LONGTEXT";
			logQuery(formatSql(sql));
			jdbcTemplate.execute(formatSql(sql));
		}

	}

	public void updateDefaultValue(Class<?> clazz, String column, String value) throws SQLException {
		String sql = "SELECT COUNT(*) FROM `" + StrUtil.toUnderlineCase(clazz.getSimpleName()) + "` WHERE `" + StrUtil.toUnderlineCase(column) + "` IS NULL";
		logQuery(formatSql(sql));
		Long count = jdbcTemplate.queryForCount(formatSql(sql));
		if (count != null && count > 0) {
			sql = "UPDATE `" + StrUtil.toUnderlineCase(clazz.getSimpleName()) + "` SET `" + StrUtil.toUnderlineCase(column) + "` = ? WHERE `" + StrUtil.toUnderlineCase(column) + "` IS NULL";
			logQuery(formatSql(sql));
			jdbcTemplate.execute(formatSql(sql), value);
		}

	}

}
