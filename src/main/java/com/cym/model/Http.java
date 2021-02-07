package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.Table;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("Http参数")
@Table
public class Http extends BaseModel {
	@ApiModelProperty("参数名")
	String name;
	@ApiModelProperty("参数值")
	String value;
	@ApiModelProperty("参数单位")
	String unit;
	@ApiModelProperty(hidden = true)
	Long seq;

	public Http() {

	}

	public Http(String name, String value, Long seq) {
		this.name = name;
		this.value = value;
		this.seq = seq;
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
