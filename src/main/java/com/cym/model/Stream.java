package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * Stream参数
 *
 */
@Table
public class Stream extends BaseModel {
	/**
	 * 参数名
	 */
	String name;
	/**
	 * 参数值
	 */
	String value;
	@JsonIgnore
	Long seq;
	
	
//	/**
//	 * IP黑白名单模式, 0:正常模式 1:黑名单模式 2:白名单模式 3:黑白名单模式
//	 */
//	@InitValue("0")
//	Integer denyAllow;
//
//	/**
//	 * 黑名单id
//	 */
//	String denyId;
//	/**
//	 * 白名单id
//	 */
//	String allowId;
//
//	public String getDenyId() {
//		return denyId;
//	}
//
//	public void setDenyId(String denyId) {
//		this.denyId = denyId;
//	}
//
//	public String getAllowId() {
//		return allowId;
//	}
//
//	public void setAllowId(String allowId) {
//		this.allowId = allowId;
//	}
//
//	public Integer getDenyAllow() {
//		return denyAllow;
//	}
//
//	public void setDenyAllow(Integer denyAllow) {
//		this.denyAllow = denyAllow;
//	}

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

	public Long getSeq() {
		return seq;
	}

	public void setSeq(Long seq) {
		this.seq = seq;
	}

}
