package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.Table;

/**
 * 
 * 静态网页
 *
 */
@Table
public class Www extends BaseModel {
	/**
	 * 路径
	 */
	String dir;

	/**
	 * 描述
	 */
	String descr;

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

}
