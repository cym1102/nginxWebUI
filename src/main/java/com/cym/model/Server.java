package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * 反向代理server
 *
 */
@Table
public class Server extends BaseModel {
	/**
	 * 监听域名
	 */
	String serverName;
	/**
	 * 监听ip端口 (格式 ip:port或port)
	 * @required
	 */
	String listen;
	/**
	 * 是否为默认server 0否(默认) 1是
	 */
	@InitValue("0")
	Integer def; 
	/**
	 * 是否http跳转https 0否(默认) 1是
	 */
	@InitValue("0")
	Integer rewrite; 
	/**
	 * http跳转https监听ip端口,默认为80 (格式 ip:port或port)
	 */
	@InitValue("80")
	String rewriteListen; 
	/**
	 * 是否开启ssl 0否(默认) 1是
	 */
	@InitValue("0")
	Integer ssl; 
	/**
	 * 是否开启http2 0否(默认) 1是
	 */
	@InitValue("0")
	Integer http2; 
	/**
	 * 是否开启proxy_protocol 0否(默认) 1是
	 */
	@InitValue("0")
	Integer proxyProtocol; 
	/**
	 * ssl证书pem文件路径
	 */
	String pem;
	/**
	 * ssl证书key文件路径
	 */
	String key;
	/**
	 * 代理类型 0:http(默认) 1:tcp 2:udp
	 */
	@InitValue("0")
	Integer proxyType;
	/**
	 * 代理upstream的id
	 */
	String proxyUpstreamId;
	@JsonIgnore
	String pemStr;
	@JsonIgnore
	String keyStr;
	/**
	 * 是否启用 true:启用(默认) false:禁用
	 */
	@InitValue("true")
	Boolean enable;
	/**
	 * 描述
	 */
	String descr; 
	/**
	 * 加密协议 (默认:TLSv1 TLSv1.1 TLSv1.2 TLSv1.3)
	 */
	@InitValue("TLSv1 TLSv1.1 TLSv1.2 TLSv1.3")
	String protocols; 
	/**
	 * 使用的password文件Id
	 */
	String passwordId;
	@JsonIgnore
	Long seq;

	public Long getSeq() {
		return seq;
	}

	public void setSeq(Long seq) {
		this.seq = seq;
	}

	public String getPasswordId() {
		return passwordId;
	}

	public void setPasswordId(String passwordId) {
		this.passwordId = passwordId;
	}

	public String getProtocols() {
		return protocols;
	}

	public void setProtocols(String protocols) {
		this.protocols = protocols;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public Integer getDef() {
		return def;
	}

	public void setDef(Integer def) {
		this.def = def;
	}

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public Integer getHttp2() {
		return http2;
	}

	public void setHttp2(Integer http2) {
		this.http2 = http2;
	}

	public String getPemStr() {
		return pemStr;
	}

	public void setPemStr(String pemStr) {
		this.pemStr = pemStr;
	}

	public String getKeyStr() {
		return keyStr;
	}

	public void setKeyStr(String keyStr) {
		this.keyStr = keyStr;
	}

	public String getProxyUpstreamId() {
		return proxyUpstreamId;
	}

	public void setProxyUpstreamId(String proxyUpstreamId) {
		this.proxyUpstreamId = proxyUpstreamId;
	}

	public Integer getProxyType() {
		return proxyType;
	}

	public void setProxyType(Integer proxyType) {
		this.proxyType = proxyType;
	}

	public Integer getSsl() {
		return ssl;
	}

	public void setSsl(Integer ssl) {
		this.ssl = ssl;
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

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getListen() {
		return listen;
	}

	public void setListen(String listen) {
		this.listen = listen;
	}

	public Integer getRewrite() {
		return rewrite;
	}

	public void setRewrite(Integer rewrite) {
		this.rewrite = rewrite;
	}

	public String getRewriteListen() {
		return rewriteListen;
	}

	public Integer getProxyProtocol() {
		return proxyProtocol;
	}

	public void setProxyProtocol(Integer proxyProtocol) {
		this.proxyProtocol = proxyProtocol;
	}

	public void setRewriteListen(String rewriteListen) {
		this.rewriteListen = rewriteListen;
	}

}
