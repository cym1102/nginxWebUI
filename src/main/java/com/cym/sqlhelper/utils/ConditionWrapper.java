package com.cym.sqlhelper.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.hutool.core.util.StrUtil;
import com.cym.config.SQLConstants;

/**
 * 查询语句生成器
 * 
 * @author CYM
 *
 */
public abstract class ConditionWrapper {
	boolean andLink;

	List<Object> list = new ArrayList<Object>();

	/**
	 * 将Wrapper转化为Condition
	 * 
	 * @param params
	 * 
	 * @return Condition
	 */
	public String build(List<String> values) {
		String sql = "";
		if (list.size() > 0) {
			List<String> blocks = new ArrayList<String>();
			for (Object object : list) {
				if (object instanceof Condition) {
					Condition condition = (Condition) object;
					String block = null;

					if (condition.getValue() == null) {
						if (condition.getOperation().equals("IS NULL") || condition.getOperation().equals("IS NOT NULL")) {
							block = buildColumn(condition.getColumn(), String.class) + " " + condition.getOperation();
						} else {
							block = buildColumn(condition.getColumn(), String.class) + " " + condition.getOperation() + " null";
						}
					} else {
						if (condition.getValue() instanceof List) {
							block = buildColumn(condition.getColumn(), condition.getValue().getClass()) + " " + condition.getOperation() + " " + buildIn(condition.getValue());
							for (Object val : (List<Object>) condition.getValue()) {
								values.add(val.toString());
							}
						} else {
							block = buildColumn(condition.getColumn(), condition.getValue().getClass()) + " " + condition.getOperation() + " ?";
							if (!condition.getOperation().equals("LIKE")) {
								values.add(condition.getValue().toString());
							} else {
								values.add("%" + condition.getValue().toString().replace("%", "\\%") + "%");
							}
						}
					}

					blocks.add(block);
				}

				if (object instanceof ConditionWrapper) {
					ConditionWrapper conditionWrapper = (ConditionWrapper) object;
					String block = " (" + conditionWrapper.build(values) + ") ";
					blocks.add(block);
				}
			}

			if (andLink) {
				sql = StrUtil.join(" AND ", blocks);
			} else {
				sql = StrUtil.join(" OR ", blocks);
			}

		}
		return sql;
	}

	public String buildColumn(String column, Class<?> clazz) {

		return SQLConstants.SUFFIX + StrUtil.toUnderlineCase(column) + SQLConstants.SUFFIX;
	}

	public String buildIn(Object value) {
		List<String> ask = new ArrayList<String>();
		for (Object obj : (Collection<?>) value) {
			ask.add("?");
		}

		if (ask.size() > 0) {
			return " (" + StrUtil.join(",", ask) + ") ";
		} else {
			return " (null) ";
		}

	}

	/**
	 * 等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionWrapper
	 */
	public ConditionWrapper eq(String column, Object params) {
		list.add(new Condition(column, "=", params));
		return this;
	}

	/**
	 * 不等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionWrapper
	 */
	public ConditionWrapper ne(String column, Object params) {
		list.add(new Condition(column, "<>", params));
		return this;
	}

	/**
	 * 小于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionWrapper
	 */
	public ConditionWrapper lt(String column, Object params) {
		list.add(new Condition(column, "<", params));
		return this;
	}

	/**
	 * 小于或等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionWrapper
	 */
	public ConditionWrapper lte(String column, Object params) {
		list.add(new Condition(column, "<=", params));
		return this;
	}

	/**
	 * 大于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionWrapper
	 */
	public ConditionWrapper gt(String column, Object params) {
		list.add(new Condition(column, ">", params));
		return this;
	}

	/**
	 * 大于或等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionWrapper
	 */
	public ConditionWrapper gte(String column, Object params) {
		list.add(new Condition(column, ">=", params));
		return this;
	}

	/**
	 * 相似于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionWrapper
	 */
	public ConditionWrapper like(String column, String params) {
		list.add(new Condition(column, "LIKE",  params ));
		return this;
	}

	/**
	 * 在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionWrapper
	 */
	public ConditionWrapper in(String column, Collection<?> params) {
		list.add(new Condition(column, "IN", params));
		return this;
	}

	/**
	 * 不在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionWrapper
	 */
	public ConditionWrapper nin(String column, Collection<?> params) {
		list.add(new Condition(column, "NOT IN", params));
		return this;
	}

	/**
	 * 为空
	 * 
	 * @param <T>
	 * 
	 * @param column 字段
	 * @return ConditionWrapper
	 */
	public ConditionWrapper isNull(String column) {
		list.add(new Condition(column, "IS NULL", null));
		return this;
	}

	/**
	 * 不为空
	 * 
	 * @param <T>
	 * 
	 * @param column 字段
	 * @return ConditionWrapper
	 */
	public ConditionWrapper isNotNull(String column) {
		list.add(new Condition(column, "IS NOT NULL", null));
		return this;
	}

	public boolean notEmpty() {
		return list.size() > 0;
	}

}
