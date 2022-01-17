package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.Table;

@Table
public class OperateLog extends BaseModel{
	// 操作员name
	String adminName;
	// 之前的配置文件
	String beforeConf;
	// 之后的配置文件
	String afterConf;


	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	public String getBeforeConf() {
		return beforeConf;
	}

	public void setBeforeConf(String beforeConf) {
		this.beforeConf = beforeConf;
	}

	public String getAfterConf() {
		return afterConf;
	}

	public void setAfterConf(String afterConf) {
		this.afterConf = afterConf;
	}
	
	
}
