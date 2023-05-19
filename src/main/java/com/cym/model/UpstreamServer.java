package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * 负载节点server
 *
 */
@Table
public class UpstreamServer extends BaseModel {
	/**
	 * 负载均衡upstream的id
	 * @required
	 */
	String upstreamId;
	/**
	 * 负载节点ip (例:10.10.10.1)
	 * @required
	 */
	String server;
	/**
	 * 负载节点端口 (例:8080)
	 * @required
	 */
	Integer port;
	/**
	 * 负载节点权重
	 */
	Integer weight;
	/**
	 * 失败等待时间,秒
	 */
	Integer failTimeout;
	/**
	 * 最大失败次数
	 */
	Integer maxFails;
	/**
	 * 最大连接数
	 */
	Integer maxConns;
	/**
	 * 状态策略 'none':无(默认) 'down':停用 'backup':备用
	 */
	@InitValue("none")
	String status;
	
	/**
	 * 其他参数
	 */
	String param;
	
	
	@JsonIgnore
	@InitValue("-1")
	Integer monitorStatus;

	
	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public Integer getMaxConns() {
		return maxConns;
	}

	public void setMaxConns(Integer maxConns) {
		this.maxConns = maxConns;
	}

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
