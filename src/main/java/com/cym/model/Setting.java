package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.CompositeIndex;
import com.cym.sqlhelper.config.SingleIndex;
import com.cym.sqlhelper.config.Table;

@Table
@CompositeIndex(colums = { "key", "value" }, unique = true)
public class Setting extends BaseModel{
	@SingleIndex(unique = true)
	String key;
	String value;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}
