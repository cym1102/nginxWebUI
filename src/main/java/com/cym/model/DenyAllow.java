package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.Table;

/**
 * 黑白名单
 * 
 * @author 陈钇蒙
 *
 */
@Table
public class DenyAllow extends BaseModel {
	Integer type; // 0 白名单 1 黑名单
	String ips; // 特例ip, 用逗号隔开
	String mark;
	
	
	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getIps() {
		return ips;
	}

	public void setIps(String ips) {
		this.ips = ips;
	}

}
