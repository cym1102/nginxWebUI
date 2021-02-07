package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.InitValue;
import cn.craccd.sqlHelper.config.Table;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("负载均衡upstream")
@Table
public class Upstream extends BaseModel {
	@ApiModelProperty("*负载均衡名称")
	String name;
	
	@ApiModelProperty("负载策略: '':无(默认) 'sticky':会话保持 'ip_hash':ip绑定 'least_conn':最少连接 'least_time':最短时间")
	String tactics; 

	@ApiModelProperty("代理类型 0:http(默认) 1:tcp/udp")
	@InitValue("0")
	Integer proxyType;

	@ApiModelProperty("监控邮件通知 0:否(默认) 1:是")
	@InitValue("0")
	Integer monitor;
	
	@ApiModelProperty(hidden = true)
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
