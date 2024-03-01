package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;

/**
 * 
 * 证书
 *
 */
@Table
public class Cert extends BaseModel {
	/**
	 * 域名
	 */
	String domain; 
	/**
	 * pem文件路径
	 */
	String pem;
	/**
	 * key文件路径
	 */
	String key;
	/**
	 * 获取方式 0 申请证书 1 手动上传 2 DNS验证
	 */
	@InitValue("0")
	Integer type; 
	
	/**
	 * 加密方式 'RSA' 'ECC'
	 */
	@InitValue("RSA")
	String encryption; 
	/**
	 * 签发时间戳
	 */
	Long makeTime; 
	/**
	 * 到期时间戳
	 */
	Long endTime; 
	/**
	 * 是否自动续签 0否 1是
	 */
	@InitValue("0")
	Integer autoRenew; 
	/**
	 * dns提供商 ali:阿里云  dp:dnsPod  cf:Cloudflare  gd:Godaddy   hw:华为云 tencent:腾讯云
	 */
	String dnsType; 
	/**
	 * dpId(dnsPod需要的参数)
	 */
	String dpId;
	/**
	 * dpKey(dnsPod需要的参数)
	 */
	String dpKey;
	
	/**
	 * tencentSecretId(腾讯云需要的参数)
	 */
	String tencentSecretId;
	/**
	 * tencentSecretKey(腾讯云需要的参数)
	 */
	String tencentSecretKey;
	/**
	 * aliKey(阿里云需要的参数)
	 */
	String aliKey;
	/**
	 * aliSecret(阿里云需要的参数)
	 */
	String aliSecret;
	/**
	 * cfEmail(Cloudflare需要的参数)
	 */
	String cfEmail;
	/**
	 * cfKey(Cloudflare需要的参数)
	 */
	String cfKey;
	
	/**
	 * gdKey(Godaddy需要的参数)
	 */
	String gdKey;
	/**
	 * gdSecret(Godaddy需要的参数)
	 */
	String gdSecret;
	/**
	 * hwUsername(华为云需要的参数)
	 */
	String hwUsername;
	/**
	 * hwPassword(华为云需要的参数)
	 */
	String hwPassword;
	/**
	 * hwProjectID(华为云需要的参数)
	 */
	String hwDomainName;
	
	
	public String getHwDomainName() {
		return hwDomainName;
	}

	public void setHwDomainName(String hwDomainName) {
		this.hwDomainName = hwDomainName;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public String getEncryption() {
		return encryption;
	}

	public void setEncryption(String encryption) {
		this.encryption = encryption;
	}

	public String getHwUsername() {
		return hwUsername;
	}

	public void setHwUsername(String hwUsername) {
		this.hwUsername = hwUsername;
	}

	public String getHwPassword() {
		return hwPassword;
	}

	public void setHwPassword(String hwPassword) {
		this.hwPassword = hwPassword;
	}


//	public String getHwProjectId() {
//		return hwProjectId;
//	}
//
//	public void setHwProjectId(String hwProjectId) {
//		this.hwProjectId = hwProjectId;
//	}

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

	public String getTencentSecretId() {
		return tencentSecretId;
	}

	public void setTencentSecretId(String tencentSecretId) {
		this.tencentSecretId = tencentSecretId;
	}

	public String getTencentSecretKey() {
		return tencentSecretKey;
	}

	public void setTencentSecretKey(String tencentSecretKey) {
		this.tencentSecretKey = tencentSecretKey;
	}


}
