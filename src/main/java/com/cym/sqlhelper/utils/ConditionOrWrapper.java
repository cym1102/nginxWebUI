package com.cym.sqlhelper.utils;

import java.util.Arrays;
import java.util.Collection;

import com.cym.sqlhelper.reflection.ReflectionUtil;
import com.cym.sqlhelper.reflection.SerializableFunction;

/**
 * 查询语句生成器 OR连接
 *
 */
public class ConditionOrWrapper extends ConditionWrapper {

	public ConditionOrWrapper() {
		andLink = false;
	}

	public ConditionOrWrapper or(ConditionWrapper conditionWrapper) {
		list.add(conditionWrapper);
		return this;
	}

	/**
	 * 等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionWrapper
	 */
	public ConditionOrWrapper eq(String column, Object params) {
		super.eq(column, params);
		return this;
	}
	/**
	 * 等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionWrapper
	 */
	public <T, R> ConditionOrWrapper eq(SerializableFunction<T, R> column, Object params) {
		super.eq(ReflectionUtil.getFieldName(column), params); 
		return this;
	}
	/**
	 * 不等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public ConditionOrWrapper ne(String column, Object params) {
		super.ne(column, params);
		return this;
	}
	
	/**
	 * 不等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public <T, R> ConditionOrWrapper ne(SerializableFunction<T, R> column, Object params) {
		super.ne(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 小于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public ConditionOrWrapper lt(String column, Object params) {
		super.lt(column, params);
		return this;
	}
	/**
	 * 小于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public <T, R> ConditionOrWrapper lt(SerializableFunction<T, R> column, Object params) {
		super.lt(ReflectionUtil.getFieldName(column), params);
		return this;
	}
	/**
	 * 小于或等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public ConditionOrWrapper lte(String column, Object params) {
		super.lte(column, params);
		return this;
	}
	/**
	 * 小于或等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public <T, R> ConditionOrWrapper lte(SerializableFunction<T, R> column, Object params) {
		super.lte(ReflectionUtil.getFieldName(column), params);
		return this;
	}
	/**
	 * 大于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public ConditionOrWrapper gt(String column, Object params) {
		super.gt(column, params);
		return this;
	}
	/**
	 * 大于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public <T, R> ConditionOrWrapper gt(SerializableFunction<T, R> column, Object params) {
		super.gt(ReflectionUtil.getFieldName(column), params);
		return this;
	}
	/**
	 * 大于或等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public ConditionOrWrapper gte(String column, Object params) {
		super.gte(column, params);
		return this;
	}
	/**
	 * 大于或等于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public <T, R> ConditionOrWrapper gte(SerializableFunction<T, R> column, Object params) {
		super.gte(ReflectionUtil.getFieldName(column), params);
		return this;
	}
	/**
	 * 相似于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public ConditionOrWrapper like(String column, String params) {
		super.like(column, params);
		return this;
	}
	/**
	 * 相似于
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */ 
	public <T, R> ConditionOrWrapper like(SerializableFunction<T, R> column, String params) {
		super.like(ReflectionUtil.getFieldName(column), params);
		return this;
	}
	/**
	 * 在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public ConditionOrWrapper in(String column, Collection<?> params) {
		super.in(column, params);
		return this;
	}
	/**
	 * 在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public <T, R> ConditionOrWrapper in(SerializableFunction<T, R> column, Collection<?> params) {
		super.in(ReflectionUtil.getFieldName(column), params);
		return this;
	}

	/**
	 * 在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public ConditionOrWrapper in(String column, Object[] params) {
		super.in(column, Arrays.asList(params));
		return this;
	}

	/**
	 * 在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public <T, R> ConditionOrWrapper in(SerializableFunction<T, R> column, Object[] params) {
		super.in(ReflectionUtil.getFieldName(column),  Arrays.asList(params));
		return this;
	}

	/**
	 * 不在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public ConditionOrWrapper nin(String column, Collection<?> params) {
		super.nin(column, params);
		return this;
	}
	/**
	 * 不在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public <T, R> ConditionOrWrapper nin(SerializableFunction<T, R> column, Collection<?> params) {
		super.nin(ReflectionUtil.getFieldName(column), params);
		return this;
	}
	
	/**
	 * 不在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public ConditionOrWrapper nin(String column, Object[] params) {
		super.nin(column, Arrays.asList(params));
		return this;
	}
	/**
	 * 不在其中
	 * 
	 * @param column 字段
	 * @param params 参数
	 * @return ConditionOrWrapper
	 */
	public <T, R> ConditionOrWrapper nin(SerializableFunction<T, R> column, Object[] params) {
		super.nin(ReflectionUtil.getFieldName(column), Arrays.asList(params));
		return this;
	}
	/**
	 * 为空
	 * 
	 * @param <T>
	 * 
	 * @param column 字段
	 * @return ConditionOrWrapper
	 */
	public ConditionOrWrapper isNull(String column) {
		super.isNull(column);
		return this;
	}
	/**
	 * 为空
	 * 
	 * @param <T>
	 * 
	 * @param column 字段
	 * @return ConditionOrWrapper
	 */
	public <T, R> ConditionOrWrapper isNull(SerializableFunction<T, R> column) {
		super.isNull(ReflectionUtil.getFieldName(column));
		return this;
	}

	/**
	 * 不为空
	 * 
	 * @param <T>
	 * 
	 * @param column 字段
	 * @return ConditionOrWrapper
	 */
	public ConditionOrWrapper isNotNull(String column) {
		super.isNotNull(column);
		return this;
	}
	/**
	 * 不为空
	 * 
	 * @param <T>
	 * 
	 * @param column 字段
	 * @return ConditionOrWrapper
	 */
	public <T, R> ConditionOrWrapper isNotNull(SerializableFunction<T, R> column) {
		super.isNotNull(ReflectionUtil.getFieldName(column));
		return this;
	}
}
