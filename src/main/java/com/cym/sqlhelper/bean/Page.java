package com.cym.sqlhelper.bean;

import java.util.Collections;
import java.util.List;

/**
 * 分页类
 * 
 */
public class Page<T> {
	/**
	 * 总记录数
	 */
	Long count = 0l;
	/**
	 * 起始页(从1开始)
	 */
	Integer curr = 1;
	/**
	 * 每页记录数
	 */
	Integer limit = 10;
	/**
	 * 列表内容
	 */
	List records = Collections.emptyList();

	public List getRecords() {
		return records;
	}

	public void setRecords(List records) {
		this.records = records;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Integer getCurr() {
		return curr;
	}

	public void setCurr(Integer curr) {
		this.curr = curr;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

}
