package com.cym.sqlhelper.utils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.bean.Sort;
import com.cym.sqlhelper.bean.Update;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.reflection.ReflectionUtil;
import com.cym.sqlhelper.reflection.SerializableFunction;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

/**
 * mongodb操作器
 *
 */
@Component
public class SqlHelper extends SqlUtils {
	@Inject("${project.beanPackage}")
	String packageName;
	@Inject
	JdbcTemplate jdbcTemplate;
	@Inject
	TableUtils tableUtils;

	static Logger logger = LoggerFactory.getLogger(SqlHelper.class);
	SnowFlake snowFlake = new SnowFlake(1, 1);
	
	@Init
	public void init() throws SQLException {
		Set<Class<?>> set = ClassUtil.scanPackage(packageName);
		for (Class<?> clazz : set) {
			tableUtils.initTable(clazz);
		}
	}

	/**
	 * 插入或更新
	 * 
	 * @param object 对象
	 */
	public String insertOrUpdate(Object object) {

		Long time = System.currentTimeMillis();
		String id = (String) ReflectUtil.getFieldValue(object, "id");
		Object objectOrg = StrUtil.isNotEmpty(id) ? findById(id, object.getClass()) : null;
		try {
			if (objectOrg == null) {
				// 插入
				// 设置插入时间
				if (ReflectUtil.getField(object.getClass(), "createTime") != null) {
					ReflectUtil.setFieldValue(object, "createTime", time);
				}
				if (ReflectUtil.getField(object.getClass(), "updateTime") != null) {
					ReflectUtil.setFieldValue(object, "updateTime", time);
				}
				// 设置默认值
				setDefaultVaule(object);

				ReflectUtil.setFieldValue(object, "id", snowFlake.nextId());

				String sql = "";
				List<String> fieldsPart = new ArrayList<String>();
				List<String> placeHolder = new ArrayList<String>();
				List<Object> paramValues = new ArrayList<Object>();

				Field[] fields = ReflectUtil.getFields(object.getClass());
				for (Field field : fields) {
					fieldsPart.add("`" + StrUtil.toUnderlineCase(field.getName()) + "`");
					placeHolder.add("?");
					paramValues.add(ReflectUtil.getFieldValue(object, field));
				}

				sql = "INSERT INTO `" + StrUtil.toUnderlineCase(object.getClass().getSimpleName()) + "` (" + StrUtil.join(",", fieldsPart) + ") VALUES (" + StrUtil.join(",", placeHolder) + ")";

				logQuery(formatSql(sql), paramValues.toArray());
				jdbcTemplate.execute(formatSql(sql), paramValues.toArray());

			} else {
				// 更新
				Field[] fields = ReflectUtil.getFields(object.getClass());

				// 设置更新时间
				if (ReflectUtil.getField(object.getClass(), "updateTime") != null) {
					ReflectUtil.setFieldValue(object, "updateTime", time);
				}

				List<String> fieldsPart = new ArrayList<String>();
				List<Object> paramValues = new ArrayList<Object>();

				for (Field field : fields) {
					if (!field.getName().equals("id") && ReflectUtil.getFieldValue(object, field) != null) {
						fieldsPart.add("`" + StrUtil.toUnderlineCase(field.getName()) + "`=?");
						paramValues.add(ReflectUtil.getFieldValue(object, field));
					}
				}
				paramValues.add(id);

				String sql = "UPDATE `" + StrUtil.toUnderlineCase(object.getClass().getSimpleName()) + "` SET " + StrUtil.join(",", fieldsPart) + " WHERE id = ?";

				logQuery(formatSql(sql), paramValues.toArray());
				jdbcTemplate.execute(formatSql(sql), paramValues.toArray());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return (String) ReflectUtil.getFieldValue(object, "id");
	}

	/**
	 * 插入
	 * 
	 * @param object 对象
	 */
	public String insert(Object object) {
		String id = (String) ReflectUtil.getFieldValue(object, "id");
		Object objectOrg = StrUtil.isNotEmpty(id) ? findById(id, object.getClass()) : null;
		if (objectOrg != null) {
			// 数据库里已有相同id, 使用新id以便插入
			ReflectUtil.setFieldValue(object, "id", snowFlake.nextId());
		}

		// 没有id生成id
		if (ReflectUtil.getFieldValue(object, "id") == null) {
			ReflectUtil.setFieldValue(object, "id", snowFlake.nextId());
		}

		insertOrUpdate(object);

		return (String) ReflectUtil.getFieldValue(object, "id");
	}

	/**
	 * 批量插入
	 * 
	 * @param <T>
	 * 
	 * @param object 对象
	 */
	public <T> void insertAll(List<T> list) {
		Long time = System.currentTimeMillis();

		Map<String, Object> idMap = new HashMap<String, Object>();
		for (Object object : list) {
			if (ReflectUtil.getFieldValue(object, "id") != null) {
				String id = (String) ReflectUtil.getFieldValue(object, "id");
				Object objectOrg = StrUtil.isNotEmpty(id) ? findById(id, object.getClass()) : null;
				idMap.put((String) ReflectUtil.getFieldValue(object, "id"), objectOrg);
			}
		}

		for (Object object : list) {
			if (ReflectUtil.getFieldValue(object, "id") != null && idMap.get((String) ReflectUtil.getFieldValue(object, "id")) != null) {
				// 数据库里已有相同id, 使用新id以便插入
				ReflectUtil.setFieldValue(object, "id", snowFlake.nextId());
			}

			// 没有id生成id
			if (ReflectUtil.getFieldValue(object, "id") == null) {
				ReflectUtil.setFieldValue(object, "id", snowFlake.nextId());
			}

			// 设置插入时间
			if (ReflectUtil.getField(object.getClass(), "createTime") != null) {
				ReflectUtil.setFieldValue(object, "createTime", time);
			}
			if (ReflectUtil.getField(object.getClass(), "updateTime") != null) {
				ReflectUtil.setFieldValue(object, "updateTime", time);
			}
			// 设置默认值
			setDefaultVaule(object);
		}

		String sqls = null;
		for (Object object : list) {
			Field[] fields = ReflectUtil.getFields(object.getClass());

			List<String> fieldsPart = new ArrayList<String>();
			List<String> placeHolder = new ArrayList<String>();

			List<Object> params = new ArrayList<Object>();
			for (Field field : fields) {
				fieldsPart.add("`" + StrUtil.toUnderlineCase(field.getName()) + "`");
				placeHolder.add("?");
				params.add(ReflectUtil.getFieldValue(object, field));
			}

			if (sqls == null) {
				sqls = "INSERT INTO `" + StrUtil.toUnderlineCase(object.getClass().getSimpleName()) + "` (" + StrUtil.join(",", fieldsPart) + ") VALUES (" + StrUtil.join(",", placeHolder) + ")";
			}

			jdbcTemplate.execute(formatSql(sqls), params.toArray());
		}

	}

	/**
	 * 根据id更新
	 * 
	 * @param object 对象
	 */
	public void updateById(Object object) {
		if (StrUtil.isEmpty((String) ReflectUtil.getFieldValue(object, "id"))) {
			return;
		}
		insertOrUpdate(object);
	}

	/**
	 * 批量更新
	 * 
	 * @param conditionAndWrapper
	 * @param update
	 * @param clazz
	 */
	public void updateMulti(ConditionWrapper conditionWrapper, Update update, Class<?> clazz) {
		if (update == null || update.getSets().size() == 0) {
			return;
		}
		List<String> fieldsPart = new ArrayList<String>();
		List<String> paramValues = new ArrayList<String>();
		for (Entry<String, Object> entry : update.getSets().entrySet()) {
			if (entry.getKey() != null && entry.getValue() != null) {
				fieldsPart.add("`" + StrUtil.toUnderlineCase(entry.getKey()) + "`=?");
				paramValues.add(entry.getValue().toString());
			}
		}

		String sql = "UPDATE `" + StrUtil.toUnderlineCase(clazz.getSimpleName()) + "` SET " + StrUtil.join(",", fieldsPart);
		if (conditionWrapper != null && conditionWrapper.notEmpty()) {
			sql += " WHERE " + conditionWrapper.build(paramValues);
		}

		logQuery(formatSql(sql), paramValues.toArray());
		jdbcTemplate.execute(formatSql(sql), paramValues.toArray());
	}

	/**
	 * 累加某一个字段的数量,原子操作
	 * 
	 * @param object
	 */
	public void addCountById(String id, String property, Long count, Class<?> clazz) {
		String sql = "UPDATE `" + StrUtil.toUnderlineCase(clazz.getSimpleName()) + "` SET `" + property + "` = CAST(`" + property + "` AS DECIMAL(30,10)) + ? WHERE `id` =  ?";
		Object[] params = new Object[] { count, id };
		logQuery(formatSql(sql), params);
		jdbcTemplate.execute(formatSql(sql), params);
	}

	/**
	 * 累加某一个字段的数量,原子操作
	 * 
	 * @param object
	 */
	public <T, R> void addCountById(String id, SerializableFunction<T, R> property, Long count, Class<?> clazz) {
		addCountById(id, ReflectionUtil.getFieldName(property), count, clazz);
	}

	/**
	 * 根据id更新
	 * 
	 * @param object 对象
	 */
	public void updateAllColumnById(Object object) {
		if (StrUtil.isEmpty((String) ReflectUtil.getFieldValue(object, "id"))) {
			return;
		}

		Field[] fields = ReflectUtil.getFields(object.getClass());

		List<String> fieldsPart = new ArrayList<String>();
		List<Object> paramValues = new ArrayList<Object>();

		for (Field field : fields) {
			if (!field.getName().equals("id")) {
				fieldsPart.add("`" + StrUtil.toUnderlineCase(field.getName()) + "`=?");
				paramValues.add(ReflectUtil.getFieldValue(object, field));
			}
		}
		paramValues.add((String) ReflectUtil.getFieldValue(object, "id"));

		String sql = "UPDATE `" + StrUtil.toUnderlineCase(object.getClass().getSimpleName()) + "` SET " + StrUtil.join(",", fieldsPart) + " WHERE id = ?";

		logQuery(formatSql(sql), paramValues.toArray());
		jdbcTemplate.execute(formatSql(sql), paramValues.toArray());

	}

	/**
	 * 根据id删除
	 * 
	 * @param id    对象
	 * @param clazz 类
	 */
	public void deleteById(String id, Class<?> clazz) {

		if (StrUtil.isEmpty(id)) {
			return;
		}
		deleteByQuery(new ConditionAndWrapper().eq("id", id), clazz);
	}

	/**
	 * 根据id删除
	 * 
	 * @param id    对象
	 * @param clazz 类
	 */
	public void deleteByIds(Collection<String> ids, Class<?> clazz) {
		if (ids == null || ids.size() == 0) {
			return;
		}

		deleteByQuery(new ConditionAndWrapper().in("id", ids), clazz);
	}

	/**
	 * 根据id删除
	 * 
	 * @param id    对象
	 * @param clazz 类
	 */
	public void deleteByIds(String[] ids, Class<?> clazz) {
		deleteByIds(Arrays.asList(ids), clazz);
	}

	/**
	 * 根据条件删除
	 * 
	 * @param query 查询
	 * @param clazz 类
	 */
	public void deleteByQuery(ConditionWrapper conditionWrapper, Class<?> clazz) {
		List<String> values = new ArrayList<String>();
		String sql = "DELETE FROM `" + StrUtil.toUnderlineCase(clazz.getSimpleName()) + "`";
		if (conditionWrapper != null && conditionWrapper.notEmpty()) {
			sql += " WHERE " + conditionWrapper.build(values);
		}
		logQuery(formatSql(sql), values.toArray());
		jdbcTemplate.execute(formatSql(sql), values.toArray());
	}

	/**
	 * 设置默认值
	 * 
	 * @param object 对象
	 */
	private void setDefaultVaule(Object object) {
		Field[] fields = ReflectUtil.getFields(object.getClass());
		for (Field field : fields) {
			// 获取注解
			if (field.isAnnotationPresent(InitValue.class)) {
				InitValue defaultValue = field.getAnnotation(InitValue.class);

				String value = defaultValue.value();

				if (ReflectUtil.getFieldValue(object, field) == null) {
					// 获取字段类型
					Class<?> type = field.getType();
					if (type.equals(String.class)) {
						ReflectUtil.setFieldValue(object, field, value);
					}
					if (type.equals(Short.class)) {
						ReflectUtil.setFieldValue(object, field, Short.parseShort(value));
					}
					if (type.equals(Integer.class)) {
						ReflectUtil.setFieldValue(object, field, Integer.parseInt(value));
					}
					if (type.equals(Long.class)) {
						ReflectUtil.setFieldValue(object, field, Long.parseLong(value));
					}
					if (type.equals(Float.class)) {
						ReflectUtil.setFieldValue(object, field, Float.parseFloat(value));
					}
					if (type.equals(Double.class)) {
						ReflectUtil.setFieldValue(object, field, Double.parseDouble(value));
					}
					if (type.equals(Boolean.class)) {
						ReflectUtil.setFieldValue(object, field, Boolean.parseBoolean(value));
					}
				}
			}
		}
	}

	/**
	 * 按查询条件获取Page
	 * 
	 * @param query 查询
	 * @param page  分页
	 * @param clazz 类
	 * @return Page 分页
	 */
	public Page findPage(ConditionWrapper conditionWrapper, Sort sort, Page page, Class<?> clazz) {
		List<String> values = new ArrayList<String>();
		// 查询出一共的条数
		Long count = findCountByQuery(conditionWrapper, clazz);

		String sql = "SELECT * FROM `" + StrUtil.toUnderlineCase(clazz.getSimpleName()) + "`";
		if (conditionWrapper != null && conditionWrapper.notEmpty()) {
			sql += " WHERE " + conditionWrapper.build(values);
		}
		if (sort != null) {
			sql += " " + sort.toString();
		} else {
			sql += " ORDER BY id DESC";
		}
		sql += " LIMIT " + (page.getCurr() - 1) * page.getLimit() + "," + page.getLimit();

		page.setCount(count);

		logQuery(formatSql(sql), values.toArray());
		page.setRecords(buildObjects(jdbcTemplate.queryForList(formatSql(sql), values.toArray()), clazz));

		return page;
	}

	/**
	 * 按查询条件获取Page
	 * 
	 * @param query 查询
	 * @param page  分页
	 * @param clazz 类
	 * @return Page 分页
	 */
	public Page findPage(Sort sort, Page page, Class<?> clazz) {
		return findPage(null, sort, page, clazz);
	}

	/**
	 * 按查询条件获取Page
	 * 
	 * @param query 查询
	 * @param page  分页
	 * @param clazz 类
	 * @return Page 分页
	 */
	public Page findPage(ConditionWrapper conditionWrapper, Page page, Class<?> clazz) {
		return findPage(conditionWrapper, null, page, clazz);
	}

	/**
	 * 按查询条件获取Page
	 * 
	 * @param query 查询
	 * @param page  分页
	 * @param clazz 类
	 * @return Page 分页
	 */
	public Page findPage(Page page, Class<?> clazz) {
		return findPage(null, null, page, clazz);
	}

	/**
	 * 根据id查找
	 * 
	 * @param id    id
	 * @param clazz 类
	 * @return T 对象
	 */
	public <T> T findById(String id, Class<T> clazz) {
		if (StrUtil.isEmpty(id)) {
			return null;
		}

		return findOneByQuery(new ConditionAndWrapper().eq("id", id), clazz);

	}

	/**
	 * 根据条件查找单个
	 * 
	 * @param query 查询
	 * @param clazz 类
	 * @return T 对象
	 */
	public <T> T findOneByQuery(ConditionWrapper conditionWrapper, Sort sort, Class<T> clazz) {
		List<String> values = new ArrayList<String>();
		List<T> list = new ArrayList<T>();
		String sql = "SELECT * FROM `" + StrUtil.toUnderlineCase(clazz.getSimpleName()) + "`";
		if (conditionWrapper != null && conditionWrapper.notEmpty()) {
			sql += " WHERE " + conditionWrapper.build(values);
		}
		if (sort != null) {
			sql += " " + sort.toString();
		} else {
			sql += " ORDER BY id DESC";
		}
		sql += " limit 1";

		logQuery(formatSql(sql), values.toArray());
		list = buildObjects(jdbcTemplate.queryForList(formatSql(sql), values.toArray()), clazz);
		return list.size() > 0 ? list.get(0) : null;
	}

	/**
	 * 根据条件查找单个
	 * 
	 * @param query 查询
	 * @param clazz 类
	 * @return T 对象
	 */
	public <T> T findOneByQuery(Sort sort, Class<T> clazz) {
		return findOneByQuery(null, sort, clazz);
	}

	/**
	 * 根据条件查找单个
	 * 
	 * @param <T>       类型
	 * @param condition
	 * @param clazz     类
	 * @return T 对象
	 */
	public <T> T findOneByQuery(ConditionWrapper conditionWrapper, Class<T> clazz) {
		return findOneByQuery(conditionWrapper, null, clazz);

	}

	/**
	 * 根据条件查找List
	 * 
	 * @param <T>   类型
	 * @param query 查询
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findListByQuery(ConditionWrapper conditionWrapper, Sort sort, Class<T> clazz) {
		List<String> values = new ArrayList<String>();

		String sql = "SELECT * FROM `" + StrUtil.toUnderlineCase(clazz.getSimpleName()) + "`";
		if (conditionWrapper != null && conditionWrapper.notEmpty()) {
			sql += " WHERE " + conditionWrapper.build(values);
		}
		if (sort != null) {
			sql += " " + sort.toString();
		} else {
			sql += " ORDER BY id DESC";
		}

		logQuery(formatSql(sql), values.toArray());
		return buildObjects(jdbcTemplate.queryForList(formatSql(sql), values.toArray()), clazz);
	}

	/**
	 * 根据条件查找List
	 * 
	 * @param <T>       类型
	 * @param condition 查询
	 * @param clazz     类
	 * @return List 列表
	 */
	public <T> List<T> findListByQuery(ConditionWrapper conditionWrapper, Class<T> clazz) {
		return (List<T>) findListByQuery(conditionWrapper, null, clazz);
	}

	/**
	 * 根据条件查找List
	 * 
	 * @param <T>       类型
	 * @param condition 查询
	 * @param clazz     类
	 * @return List 列表
	 */
	public <T> List<T> findListByQuery(Sort sort, Class<T> clazz) {
		return (List<T>) findListByQuery(null, sort, clazz);
	}

	/**
	 * 根据条件查找某个属性
	 * 
	 * @param <T>           类型
	 * @param query         查询
	 * @param documentClass 类
	 * @param property      属性
	 * @param propertyClass 属性类
	 * @return List 列表
	 */
	public <T> List<T> findPropertiesByQuery(ConditionWrapper conditionWrapper, Class<?> documentClass, String property, Class<T> propertyClass) {
		List<?> list = findListByQuery(conditionWrapper, documentClass);
		List<T> propertyList = extractProperty(list, property, propertyClass);

		return propertyList;
	}

	/**
	 * 根据条件查找某个属性
	 * 
	 * @param <T>           类型
	 * @param query         查询
	 * @param documentClass 类
	 * @param property      属性
	 * @param propertyClass 属性类
	 * @return List 列表
	 */
	public <T, R> List<T> findPropertiesByQuery(ConditionWrapper conditionWrapper, Class<?> documentClass, SerializableFunction<T, R> property, Class<T> propertyClass) {
		return findPropertiesByQuery(conditionWrapper, documentClass, ReflectionUtil.getFieldName(property), propertyClass);
	}

	/**
	 * 根据条件查找某个属性
	 * 
	 * @param <T>           类型
	 * @param condition     查询
	 * @param documentClass 类
	 * @param property      属性
	 * @return List 列表
	 */
	public List<String> findPropertiesByQuery(ConditionWrapper conditionWrapper, Class<?> documentClass, String property) {
		return findPropertiesByQuery(conditionWrapper, documentClass, property, String.class);
	}

	/**
	 * 根据条件查找某个属性
	 * 
	 * @param <T>           类型
	 * @param condition     查询
	 * @param documentClass 类
	 * @param property      属性
	 * @return List 列表
	 */
	public <T, R> List<String> findPropertiesByQuery(ConditionWrapper conditionWrapper, Class<?> documentClass, SerializableFunction<T, R> property) {
		return findPropertiesByQuery(conditionWrapper, documentClass, ReflectionUtil.getFieldName(property), String.class);
	}

	/**
	 * 根据id查找某个属性
	 * 
	 * @param <T>           类型
	 * @param condition     查询
	 * @param documentClass 类
	 * @param property      属性
	 * @return List 列表
	 */
	public List<String> findPropertiesByIds(Collection<String> ids, Class<?> documentClass, String property) {
		if (ids == null || ids.size() == 0) {
			return new ArrayList<String>();
		}

		ConditionAndWrapper ConditionAndWrapper = new ConditionAndWrapper();
		ConditionAndWrapper.in("id", ids);

		return findPropertiesByQuery(ConditionAndWrapper, documentClass, property, String.class);
	}

	/**
	 * 根据id查找某个属性
	 * 
	 * @param <T>           类型
	 * @param condition     查询
	 * @param documentClass 类
	 * @param property      属性
	 * @return List 列表
	 */
	public <T, R> List<String> findPropertiesByIds(Collection<String> ids, Class<?> documentClass, SerializableFunction<T, R> property) {
		return findPropertiesByIds(ids, documentClass, ReflectionUtil.getFieldName(property));
	}

	/**
	 * 根据id查找某个属性
	 * 
	 * @param <T>           类型
	 * @param condition     查询
	 * @param documentClass 类
	 * @param property      属性
	 * @return List 列表
	 */
	public List<String> findPropertiesByIds(String[] ids, Class<?> documentClass, String property) {
		return findPropertiesByIds(Arrays.asList(ids), documentClass, property);
	}

	/**
	 * 根据id查找某个属性
	 * 
	 * @param <T>           类型
	 * @param condition     查询
	 * @param documentClass 类
	 * @param property      属性
	 * @return List 列表
	 */
	public <T, R> List<String> findPropertiesByIds(String[] ids, Class<?> documentClass, SerializableFunction<T, R> property) {
		return findPropertiesByIds(Arrays.asList(ids), documentClass, ReflectionUtil.getFieldName(property));
	}

	/**
	 * 根据条件查找id
	 * 
	 * @param query 查询
	 * @param clazz 类
	 * @return List 列表
	 */
	public List<String> findIdsByQuery(ConditionWrapper conditionWrapper, Class<?> clazz) {

		return findPropertiesByQuery(conditionWrapper, clazz, "id");
	}

	/**
	 * 根据id集合查找
	 * 
	 * @param List  ids id集合
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findListByIds(Collection<String> ids, Class<T> clazz) {
		return findListByIds(ids, null, clazz);
	}

	/**
	 * 根据id集合查找
	 * 
	 * @param List  ids id集合
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findListByIds(String[] ids, Class<T> clazz) {
		return findListByIds(Arrays.asList(ids), null, clazz);
	}

	/**
	 * 根据id集合查找
	 * 
	 * @param List  ids id集合
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findListByIds(Collection<String> ids, Sort sort, Class<T> clazz) {
		if (ids == null || ids.size() == 0) {
			return new ArrayList<T>();
		}

		ConditionAndWrapper ConditionAndWrapper = new ConditionAndWrapper();
		ConditionAndWrapper.in("id", ids);

		return findListByQuery(ConditionAndWrapper, sort, clazz);
	}

	/**
	 * 根据id集合查找
	 * 
	 * @param List  ids id集合
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findListByIds(String[] ids, Sort sort, Class<T> clazz) {
		return findListByIds(Arrays.asList(ids), sort, clazz);
	}

	/**
	 * 查询全部
	 * 
	 * @param <T>   类型
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findAll(Class<T> clazz) {
		return findAll(null, clazz);
	}

	/**
	 * 查询全部
	 * 
	 * @param <T>   类型
	 * @param clazz 类
	 * @return List 列表
	 */
	public <T> List<T> findAll(Sort sort, Class<T> clazz) {
		return findListByQuery(null, sort, clazz);
	}

	/**
	 * 查找全部的id
	 * 
	 * @param clazz 类
	 * @return List 列表
	 */
	public List<String> findAllIds(Class<?> clazz) {
		return findIdsByQuery(null, clazz);
	}

	/**
	 * 查找数量
	 * 
	 * @param condition 查询
	 * @param clazz     类
	 * @return Long 数量
	 */
	public Long findCountByQuery(ConditionWrapper conditionWrapper, Class<?> clazz) {
		List<String> values = new ArrayList<String>();
		String sql = "SELECT COUNT(*) FROM `" + StrUtil.toUnderlineCase(clazz.getSimpleName()) + "`";
		if (conditionWrapper != null && conditionWrapper.notEmpty()) {
			sql += " WHERE " + conditionWrapper.build(values);
		}

		logQuery(formatSql(sql), values.toArray());
		return jdbcTemplate.queryForCount(formatSql(sql), values.toArray());
	}

	/**
	 * 查找全部数量
	 * 
	 * @param clazz 类
	 * @return Long 数量
	 */
	public Long findAllCount(Class<?> clazz) {
		return findCountByQuery(null, clazz);
	}

	/**
	 * 获取list中对象某个属性,组成新的list
	 * 
	 * @param list     列表
	 * @param clazz    类
	 * @param property 属性
	 * @return List<T> 列表
	 */
	private <T> List<T> extractProperty(List<?> list, String property, Class<T> clazz) {
		Set<T> rs = new HashSet<T>();
		for (Object object : list) {
			Object value = ReflectUtil.getFieldValue(object, property);
			if (value != null && value.getClass().equals(clazz)) {
				rs.add((T) value);
			}
		}

		return new ArrayList<T>(rs);
	}

	/**
	 * Map转Bean
	 * 
	 * @param <T>
	 * @param queryForList
	 * @param clazz
	 * @return
	 */
	private <T> List<T> buildObjects(List<Map<String, Object>> queryForList, Class<T> clazz) {
		List<T> list = new ArrayList<T>();
		try {

			Field[] fields = ReflectUtil.getFields(clazz);

			for (Map<String, Object> map : queryForList) {
				Object obj = clazz.getDeclaredConstructor().newInstance();

				for (Map.Entry<String, Object> entry : map.entrySet()) {
					String mapKey = entry.getKey();
					Object mapValue = entry.getValue();

					for (Field field : fields) {
						if (StrUtil.toUnderlineCase(field.getName()).equals(mapKey)) {
							ReflectUtil.setFieldValue(obj, field.getName(), mapValue);
							break;
						}
					}

				}

				list.add((T) obj);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

}
