package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;

@Table
public class Remote extends BaseModel{
	String protocol;
	String ip;
	Integer port;
	@InitValue("0")
	Integer status; // 0 掉线 1在线
	String creditKey;
	String name;
	String pass;
	String version;
	String system;
	String descr;
	@InitValue("0")
	Integer monitor;
	
	String parentId;
	Integer type; // 0服务器 1分组
	Integer nginx; //0未运行 1在运行 2未知
	
	Boolean select;
	
//	Boolean isParent;// tree控件需要用到
//	
//	
//	
//	
//	public Boolean getIsParent() {
//		return isParent;
//	}
//
//	public void setIsParent(Boolean isParent) {
//		this.isParent = isParent;
//	}

	public Boolean getSelect() {
		return select;
	}

	public void setSelect(Boolean select) {
		this.select = select;
	}

	public Integer getMonitor() {
		return monitor;
	}

	public void setMonitor(Integer monitor) {
		this.monitor = monitor;
	}

	public Integer getNginx() {
		return nginx;
	}

	public void setNginx(Integer nginx) {
		this.nginx = nginx;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
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


	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getCreditKey() {
		return creditKey;
	}

	public void setCreditKey(String creditKey) {
		this.creditKey = creditKey;
	}


}
