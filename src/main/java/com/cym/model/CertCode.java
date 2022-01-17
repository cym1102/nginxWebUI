package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.Table;
/**
 * 
 * 证书解析码
 *
 */
@Table
public class CertCode extends BaseModel {
	/**
	 * 证书id
	 */
	String certId; 
	/**
	 * 域名
	 */
	String domain;
	/**
	 * 解析类型
	 */
	String type;
	/**
	 * 解析值
	 */
	String value;
	
	public String getCertId() {
		return certId;
	}
	public void setCertId(String certId) {
		this.certId = certId;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	} 


	
	
}
