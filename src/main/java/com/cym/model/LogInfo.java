package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.Table;

@Table
public class LogInfo extends BaseModel {
	String remoteAddr;
	String remoteUser;
	String timeLocal;
	String request;
	String httpHost;
	String status;
	String requestLength;
	String bodyBytesDent;
	String httpReferer;
	String httpUserAgent;
	String requestTime;
	String upstreamResponseTime;

	String hour;
	String minute;
	String second;


	public String getHour() {
		return hour;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public String getMinute() {
		return minute;
	}

	public void setMinute(String minute) {
		this.minute = minute;
	}

	public String getSecond() {
		return second;
	}

	public void setSecond(String second) {
		this.second = second;
	}

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public String getTimeLocal() {
		return timeLocal;
	}

	public void setTimeLocal(String timeLocal) {
		this.timeLocal = timeLocal;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getHttpHost() {
		return httpHost;
	}

	public void setHttpHost(String httpHost) {
		this.httpHost = httpHost;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRequestLength() {
		return requestLength;
	}

	public void setRequestLength(String requestLength) {
		this.requestLength = requestLength;
	}

	public String getBodyBytesDent() {
		return bodyBytesDent;
	}

	public void setBodyBytesDent(String bodyBytesDent) {
		this.bodyBytesDent = bodyBytesDent;
	}

	public String getHttpReferer() {
		return httpReferer;
	}

	public void setHttpReferer(String httpReferer) {
		this.httpReferer = httpReferer;
	}

	public String getHttpUserAgent() {
		return httpUserAgent;
	}

	public void setHttpUserAgent(String httpUserAgent) {
		this.httpUserAgent = httpUserAgent;
	}

	public String getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}

	public String getUpstreamResponseTime() {
		return upstreamResponseTime;
	}

	public void setUpstreamResponseTime(String upstreamResponseTime) {
		this.upstreamResponseTime = upstreamResponseTime;
	}

}
