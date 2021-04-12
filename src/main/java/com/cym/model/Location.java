package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.InitValue;
import cn.craccd.sqlHelper.config.Table;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("代理目标location")
@Table
public class Location extends BaseModel {
	@ApiModelProperty("*所属反向代理serverId")
	String serverId;
	@ApiModelProperty("*监控路径 例:/")
	String path;
	@ApiModelProperty("代理类型 0:动态代理(默认) 1:静态代理 2:负载均衡 3:空白代理")
	@InitValue("0")
	Integer type;
	@ApiModelProperty(hidden = true, name = "额外参数")
	String locationParamJson;

	@ApiModelProperty("动态代理目标 (例:http://10.10.10.1:8080/)")
	String value;

	@ApiModelProperty("代理负载协议,http or https")
	@InitValue("http")
	String upstreamType;
	@ApiModelProperty("代理负载均衡upstream的id")
	String upstreamId; 
	@ApiModelProperty("代理负载额外路径,默认为空")
	String upstreamPath;

	@ApiModelProperty("静态代理路径 (例:/home/www)")
	String rootPath;
	@ApiModelProperty("静态代理默认页面 (例:index.html)")
	String rootPage;
	@ApiModelProperty("静态代理类型 root:根路径模式 alias:别名模式")
	String rootType;

	@ApiModelProperty("是否携带Host参数 0否 1是(默认)")
	@InitValue("1")
	Integer header; 
	
	@ApiModelProperty("是否开启websocket支持 0否(默认) 1是")
	@InitValue("0")
	Integer websocket;

	public String getUpstreamType() {
		return upstreamType;
	}

	public void setUpstreamType(String upstreamType) {
		this.upstreamType = upstreamType;
	}

	public Integer getWebsocket() {
		return websocket;
	}

	public void setWebsocket(Integer websocket) {
		this.websocket = websocket;
	}

	public Integer getHeader() {
		return header;
	}

	public void setHeader(Integer header) {
		this.header = header;
	}

	public String getRootType() {
		return rootType;
	}

	public void setRootType(String rootType) {
		this.rootType = rootType;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getRootPage() {
		return rootPage;
	}

	public void setRootPage(String rootPage) {
		this.rootPage = rootPage;
	}

	public String getUpstreamPath() {
		return upstreamPath;
	}

	public void setUpstreamPath(String upstreamPath) {
		this.upstreamPath = upstreamPath;
	}

	public String getLocationParamJson() {
		return locationParamJson;
	}

	public void setLocationParamJson(String locationParamJson) {
		this.locationParamJson = locationParamJson;
	}

	public String getUpstreamId() {
		return upstreamId;
	}

	public void setUpstreamId(String upstreamId) {
		this.upstreamId = upstreamId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
