package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.InitValue;
import cn.craccd.sqlHelper.config.Table;

@Table
public class Upstream extends BaseModel {
	String name;
	String tactics; // 负载策略

	// 代理类型
	@InitValue("0")
	Integer proxyType; // 0 http 1 tcp/udp

	@InitValue("0")
	Integer monitor;

	Long seq;

	public Long getSeq() {
		return seq;
	}

	public void setSeq(Long seq) {
		this.seq = seq;
	}

	public Integer getMonitor() {
		return monitor;
	}

	public void setMonitor(Integer monitor) {
		this.monitor = monitor;
	}

	public Integer getProxyType() {
		return proxyType;
	}

	public void setProxyType(Integer proxyType) {
		this.proxyType = proxyType;
	}

	public String getTactics() {
		return tactics;
	}

	public void setTactics(String tactics) {
		this.tactics = tactics;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
