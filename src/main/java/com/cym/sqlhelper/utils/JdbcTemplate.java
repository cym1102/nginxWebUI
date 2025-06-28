package com.cym.sqlhelper.utils;

import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.SQLConstants;
import com.cym.sqlhelper.config.DataSourceEmbed;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;

@Component
public class JdbcTemplate {
	@Inject
	DataSourceEmbed dataSourceEmbed; 
	SnowFlake snowFlake = new SnowFlake(1, 1);
	static Logger logger = LoggerFactory.getLogger(JdbcTemplate.class);
	
	public List<Map<String, Object>> queryForList(String formatSql, Object... array) {
		try {
//			System.out.println(">>>queryForList sql:"+formatSql+" array:"+ Arrays.toString(array));
			List<Entity> list = Db.use(dataSourceEmbed.getDataSource()).query(formatSql, array);

			List<Map<String, Object>> mapList = new ArrayList<>();
			for (Entity entity : list) {
				Map<String, Object> map = new HashMap<>();
				for (Map.Entry entry : entity.entrySet()) {
//					if (entry.getValue() instanceof JdbcClob) {
//						map.put(entry.getKey().toString(), clobToStr((JdbcClob) entry.getValue()));
//					} else {
						map.put(entry.getKey().toString(), entry.getValue());
//					}

				}
				mapList.add(map);
			}

			return mapList;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

//	public String clobToStr(JdbcClob jdbcClob) {
//		try {
//			StringBuilder builder = new StringBuilder();
//			Reader rd = jdbcClob.getCharacterStream();
//			char[] str = new char[1];
//			while (rd.read(str) != -1) {
//				builder.append(new String(str));
//			}
//			return builder.toString();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}

	public Set<String> queryForColumn(Class clazz) throws SQLException {
		Set<String> set = new HashSet<>();
		String uuid = snowFlake.nextId();
		Entity entity = new Entity();
		entity.setTableName(StrUtil.toUnderlineCase(clazz.getSimpleName()));
		entity.set("id", uuid);
		Db.use(dataSourceEmbed.getDataSource()).insert(entity);
		List<Entity> list = Db.use(dataSourceEmbed.getDataSource()).query("select * from " + SQLConstants.SUFFIX + StrUtil.toUnderlineCase(clazz.getSimpleName()) + SQLConstants.SUFFIX + " where id='" + uuid + "'");

		for (Entity entityOne : list) {
			set = entityOne.getFieldNames();
		}
		Db.use(dataSourceEmbed.getDataSource()).del(entity);

		return set;
	}

	public Long queryForCount(String formatSql, Object... array) {
		List<Map<String, Object>> list = queryForList(formatSql, array);
		if (list != null && list.size() != 0) {
			Map<String, Object> map = list.get(0);
			for (Entry<String, Object> entity : map.entrySet()) {
				if (entity.getValue() instanceof Long) {
					return (Long) entity.getValue();
				}
				if (entity.getValue() instanceof Integer) {
					return ((Integer) entity.getValue()).longValue();
				}
				if (entity.getValue() instanceof Short) {
					return ((Short) entity.getValue()).longValue();
				}
			}
		}

		return 0l;
	}

	public void execute(String formatSql, Object... array) {
		try {
//			System.out.println(">>>execute sql:"+formatSql);
			Db.use(dataSourceEmbed.getDataSource()).execute(formatSql, array);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

	}

}
