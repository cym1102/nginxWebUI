package com.cym.ext;

import java.util.List;

import com.cym.model.Param;
import com.cym.model.Template;

public class TemplateExt {
	Template template;
	Integer count;
	List<Param> paramList;
	
	
	public List<Param> getParamList() {
		return paramList;
	}
	public void setParamList(List<Param> paramList) {
		this.paramList = paramList;
	}
	public Template getTemplate() {
		return template;
	}
	public void setTemplate(Template template) {
		this.template = template;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	
	
}
