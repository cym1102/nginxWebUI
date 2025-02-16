package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * 代理目标location
 *
 */
@Table
public class Location extends BaseModel {
	/**
	 * 所属反向代理serverId
	 * 
	 * @required
	 */
	String serverId;
	/**
	 * 监控路径 例:/
	 * 
	 * @required
	 */
	String path;
	/**
	 * 代理类型 0:动态代理(默认) 1:静态代理 2:负载均衡 3:空白代理 4:重定向
	 */
	@InitValue("0")
	Integer type;

	@JsonIgnore
	String locationParamJson;

	/**
	 * 重定向路径
	 */
	String returnUrl;

	/**
	 * 重定向是否携带源路径 0否 1是(默认)
	 */
	@InitValue("1")
	Integer returnPath;

	/**
	 * 动态代理目标 (例:http://10.10.10.1:8080/)
	 */
	String value;
	/**
	 * 代理负载协议,http or https
	 */
	@InitValue("http")
	String upstreamType;
	/**
	 * 代理负载均衡upstream的id
	 */
	String upstreamId;
	/**
	 * 代理负载额外路径,默认为空
	 */
	String upstreamPath;
	/**
	 * 静态代理路径 (例:/home/www)
	 */
	String rootPath;
	/**
	 * 静态代理默认页面 (例:index.html)
	 */
	String rootPage;
	/**
	 * 静态代理类型 root:根路径模式 alias:别名模式
	 */
	String rootType;
	/**
	 * 是否携带Host参数 0否 1是(默认)
	 */
	@InitValue("1")
	Integer header;
	/**
	 * Host参数类型 $host(默认) $http_host $host:$proxy_port $host:$server_port
	 */
	@InitValue("$host")
	String headerHost;

	/**
	 * 是否开启websocket支持 0否(默认) 1是
	 */
	@InitValue("0")
	Integer websocket;

	/**
	 * 是否开启跨域支持 0否(默认) 1是
	 */
	@InitValue("0")
	Integer cros;

	/**
	 * 描述
	 */
	String descr;
	/**
	 * 是否启用 1:启用(默认) 0:禁用
	 */
	@InitValue("1")
	Integer enable;
	
	


	public Integer getEnable() {
		return enable;
	}

	public void setEnable(Integer enable) {
		this.enable = enable;
	}

	public Integer getReturnPath() {
		return returnPath;
	}

	public void setReturnPath(Integer returnPath) {
		this.returnPath = returnPath;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public Integer getCros() {
		return cros;
	}

	public void setCros(Integer cros) {
		this.cros = cros;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

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

	public String getHeaderHost() {
		return headerHost;
	}

	public void setHeaderHost(String headerHost) {
		this.headerHost = headerHost;
	}

}
