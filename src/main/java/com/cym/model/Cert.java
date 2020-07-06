package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.InitValue;
import cn.craccd.sqlHelper.config.SingleIndex;
import cn.craccd.sqlHelper.config.Table;

@Table
public class Cert extends BaseModel {
	@SingleIndex(unique = true)
	String domain;// 域名

	String pem;
	String key;

	@InitValue("0")
	Integer type; // 获取方式 0 申请证书 1 手动上传

	Long makeTime; // 生成时间

	@InitValue("0")
	Integer autoRenew; // 自动续签

	String pemStr;
	String keyStr;

	String dnsType;
	String dpId;
	String dpKey;
	String aliKey;
	String aliSecret;

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getDnsType() {
		return dnsType;
	}

	public void setDnsType(String dnsType) {
		this.dnsType = dnsType;
	}

	public String getDpId() {
		return dpId;
	}

	public void setDpId(String dpId) {
		this.dpId = dpId;
	}

	public String getDpKey() {
		return dpKey;
	}

	public void setDpKey(String dpKey) {
		this.dpKey = dpKey;
	}

	public String getAliKey() {
		return aliKey;
	}

	public void setAliKey(String aliKey) {
		this.aliKey = aliKey;
	}

	public String getAliSecret() {
		return aliSecret;
	}

	public void setAliSecret(String aliSecret) {
		this.aliSecret = aliSecret;
	}

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
