package com.cym.sqlhelper.utils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Set;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;

import cn.hutool.core.util.ReflectUtil;

@Component
public class TableUtils {
	static Logger logger = LoggerFactory.getLogger(TableUtils.class);

	@Inject
	JdbcTemplate jdbcTemplate;
	
	@Inject
	SqlUtils sqlUtils;

	public void initTable(Class<?> clazz) throws SQLException {
		Table table = clazz.getAnnotation(Table.class);
		if (table != null) {
			// 创建表
			sqlUtils.checkOrCreateTable(clazz);

			// 获取表所有字段
			Set<String> columns = jdbcTemplate.queryForColumn(clazz);

			// 建立字段
			Field[] fields = ReflectUtil.getFields(clazz);
			for (Field field : fields) {
				// 创建字段
				if (!field.getName().equals("id")) {
					sqlUtils.checkOrCreateColumn(clazz, field.getName(), columns);
				}

				// 更新表默认值
				if (field.isAnnotationPresent(InitValue.class)) {
					InitValue defaultValue = field.getAnnotation(InitValue.class);
					if (defaultValue.value() != null) {
						sqlUtils.updateDefaultValue(clazz, field.getName(), defaultValue.value());
					}
				}
			}

		}
	}

}
