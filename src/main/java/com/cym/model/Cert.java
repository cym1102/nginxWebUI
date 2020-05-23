package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.InitValue;
import cn.craccd.sqlHelper.config.SingleIndex;
import cn.craccd.sqlHelper.config.Table;

@Table
public class Cert extends BaseModel{
	@SingleIndex(unique = true)
	String domain;//域名
	
	String pem;
	String key;
	
	Long makeTime; // 生成时间 
	
	@InitValue("0")
	Integer autoRenew; // 自动续签

	String pemStr;
	String keyStr;
	
	
	
	public String getPemStr() {
		return pemStr;
	}
	public void setPemStr(String pemStr) {
		this.pemStr = pemStr;
	}
	public String getKeyStr() {
		return keyStr;
	}
	public void setKeyStr(String keyStr) {
		this.keyStr = keyStr;
	}
	public Integer getAutoRenew() {
		return autoRenew;
	}
	public void setAutoRenew(Integer autoRenew) {
		this.autoRenew = autoRenew;
	}
	public Long getMakeTime() {
		return makeTime;
	}
	public void setMakeTime(Long makeTime) {
		this.makeTime = makeTime;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getPem() {
		return pem;
	}
	public void setPem(String pem) {
		this.pem = pem;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	
}
