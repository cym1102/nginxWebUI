package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.Table;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("密码文件")
@Table
public class Password extends BaseModel {
	@ApiModelProperty("用户名")
	String name;
	@ApiModelProperty("密码")
	String pass;
	@ApiModelProperty(hidden = true, name = "文件路径")
	String path;
	@ApiModelProperty("描述")
	String descr;
	@ApiModelProperty(hidden = true, name = "文件内容")
	String pathStr;

	public String getPathStr() {
		return pathStr;
	}

	public void setPathStr(String pathStr) {
		this.pathStr = pathStr;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
