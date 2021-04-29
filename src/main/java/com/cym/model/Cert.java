package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.InitValue;
import cn.craccd.sqlHelper.config.SingleIndex;
import cn.craccd.sqlHelper.config.Table;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@ApiModel("证书")
@Table
public class Cert extends BaseModel {
	@ApiModelProperty("域名")
	@SingleIndex(unique = true)
	String domain; 
	@ApiModelProperty("pem文件路径")
	String pem;
	@ApiModelProperty("key文件路径")
	String key;

	@ApiModelProperty("获取方式 0 申请证书 1 手动上传")
	@InitValue("0")
	Integer type; 

	@ApiModelProperty("签发时间戳")
	Long makeTime; 

	@ApiModelProperty("是否自动续签 0否 1是")
	@InitValue("0")
	Integer autoRenew; 

//	@ApiModelProperty("pem文件内容")
//	String pemStr;
//	
//	@ApiModelProperty("key文件内容")
//	String keyStr;

	@ApiModelProperty("dns提供商 ali:阿里云  dp:腾讯云  cf:Cloudflare  gd:Godaddy")
	String dnsType; 
	@ApiModelProperty("dpId(腾讯云需要的参数)")
	String dpId;
	@ApiModelProperty("dpKey(腾讯云需要的参数)")
	String dpKey;
	@ApiModelProperty("aliKey(阿里云需要的参数)")
	String aliKey;
	@ApiModelProperty("aliSecret(阿里云需要的参数)")
	String aliSecret;
	@ApiModelProperty("cfEmail(Cloudflare需要的参数)")
	String cfEmail;
	@ApiModelProperty("cfKey(Cloudflare需要的参数)")
	String cfKey;
	@ApiModelProperty("gdKey(Godaddy需要的参数)")
	String gdKey;
	@ApiModelProperty("gdSecret(Godaddy需要的参数)")
	String gdSecret;
	
	public String getGdKey() {
		return gdKey;
	}

	public void setGdKey(String gdKey) {
		this.gdKey = gdKey;
	}

	public String getGdSecret() {
		return gdSecret;
	}

	public void setGdSecret(String gdSecret) {
		this.gdSecret = gdSecret;
	}

	public String getCfEmail() {
		return cfEmail;
	}

	public void setCfEmail(String cfEmail) {
		this.cfEmail = cfEmail;
	}

	public String getCfKey() {
		return cfKey;
	}

	public void setCfKey(String cfKey) {
		this.cfKey = cfKey;
	}

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

//	public String getPemStr() {
//		return pemStr;
//	}
//
//	public void setPemStr(String pemStr) {
//		this.pemStr = pemStr;
//	}
//
//	public String getKeyStr() {
//		return keyStr;
//	}
//
//	public void setKeyStr(String keyStr) {
//		this.keyStr = keyStr;
//	}

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
