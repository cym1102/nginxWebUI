package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.Table;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("额外参数")
@Table
public class Param extends BaseModel {
	@ApiModelProperty("所属反向代理id")
	String serverId;
	@ApiModelProperty("所属代理目标id")
	String locationId;
	@ApiModelProperty("所属负载均衡id")
	String upstreamId;
	@ApiModelProperty(hidden = true)
	String templateId;
	
	@ApiModelProperty("参数名")
	String name;
	@ApiModelProperty("参数值")
	String value;
	@ApiModelProperty(hidden = true)
	String templateValue;
	@ApiModelProperty(hidden = true)
	String templateName;
	
	
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
