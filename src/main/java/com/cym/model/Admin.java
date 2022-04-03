package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;

@Table
public class Admin extends BaseModel {
	String name;
	String pass;
	// 谷歌秘钥
	String key;
	// 是否开启谷歌验证
	@InitValue("false")
	Boolean auth;

	// 是否开启api
	@InitValue("false")
	Boolean api;

	String token;

	// 类型 0 超管 1 受限用户
	@InitValue("0")
	Integer type;
	
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Boolean getApi() {
		return api;
	}

	public void setApi(Boolean api) {
		this.api = api;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Boolean getAuth() {
		return auth;
	}

	public void setAuth(Boolean auth) {
		this.auth = auth;
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

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
