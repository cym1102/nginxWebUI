package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.SingleIndex;
import com.cym.sqlhelper.config.Table;

@Table
public class Www extends BaseModel {
	@SingleIndex(unique = true)
	String dir;
	
//	String name;
//
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

}
