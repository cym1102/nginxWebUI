package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * 额外参数
 *
 */
@Table
public class Param extends BaseModel {
	/**
	 * 所属反向代理id
	 */
	String serverId;
	/**
	 * 所属代理目标id
	 */
	String locationId;
	/**
	 * 所属负载均衡id
	 */
	String upstreamId;

	@JsonIgnore
	String templateId;
	/**
	 * 参数名
	 */
	String name;
	/**
	 * 参数值
	 */
	String value;
	@JsonIgnore
	String templateValue;
	@JsonIgnore
	String templateName;

	/**
	 * 参数位置 0:追加到末尾(默认) 1:插入到开头
	 */
	Integer position;

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getTemplateValue() {
		return templateValue;
	}

	public void setTemplateValue(String templateValue) {
		this.templateValue = templateValue;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getUpstreamId() {
		return upstreamId;
	}

	public void setUpstreamId(String upstreamId) {
		this.upstreamId = upstreamId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
