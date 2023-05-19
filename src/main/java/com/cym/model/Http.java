package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Http参数
 *
 */
@Table
public class Http extends BaseModel {
	/**
	 * 参数名
	 */
	String name;
	/**
	 * 参数值
	 */
	String value;
	/**
	 * 参数单位
	 */
	String unit;
	@JsonIgnore
	Long seq;

	/**
	 * 是否启用 true:启用(默认) false:禁用
	 */
	@InitValue("true")
	Boolean enable;
	
	public Http() {

	}

	public Http(String name, String value, Long seq) {
		this.name = name;
		this.value = value;
		this.seq = seq;
	}

	
	
	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public Long getSeq() {
		return seq;
	}

	public void setSeq(Long seq) {
		this.seq = seq;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
