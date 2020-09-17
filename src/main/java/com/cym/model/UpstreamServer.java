package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.InitValue;
import cn.craccd.sqlHelper.config.Table;

@Table
public class UpstreamServer extends BaseModel {
	String upstreamId;
	String server;
	Integer port;
	Integer weight;
	
	Integer failTimeout; // 失败超时
	Integer maxFails; // 失败次数
	
	String status; // 状态策略
	
	@InitValue("-1")
	Integer monitorStatus; // 监控状态 -1未检测 0 不通 1 通
	
	
	public Integer getMonitorStatus() {
		return monitorStatus;
	}
	public void setMonitorStatus(Integer monitorStatus) {
		this.monitorStatus = monitorStatus;
	}
	public Integer getFailTimeout() {
		return failTimeout;
	}
	public void setFailTimeout(Integer failTimeout) {
		this.failTimeout = failTimeout;
	}
	public Integer getMaxFails() {
		return maxFails;
	}
	public void setMaxFails(Integer maxFails) {
		this.maxFails = maxFails;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getUpstreamId() {
		return upstreamId;
	}
	public void setUpstreamId(String upstreamId) {
		this.upstreamId = upstreamId;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public Integer getWeight() {
		return weight;
	}
	public void setWeight(Integer weight) {
		this.weight = weight;
	}
	
	
}
