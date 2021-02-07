package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.InitValue;
import cn.craccd.sqlHelper.config.Table;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("Stream参数")
@Table
public class Stream extends BaseModel {
	@ApiModelProperty("参数名")
	String name;
	@ApiModelProperty("参数值")
	String value;
	@ApiModelProperty(hidden = true)
	Long seq;

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
