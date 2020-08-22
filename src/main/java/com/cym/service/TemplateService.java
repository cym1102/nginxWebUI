package com.cym.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cym.model.Param;
import com.cym.model.Template;

import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;

@Service
public class TemplateService {
	@Autowired
	SqlHelper sqlHelper;

	@Transactional
	public void addOver(Template template, List<Param> params) {
		sqlHelper.insertOrUpdate(template);

		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("templateId", template.getId()), Param.class);
		// 反向插入,保证列表与输入框对应
		Collections.reverse(params);
		for (Param param : params) {
			param.setTemplateId(template.getId());
			sqlHelper.insertOrUpdate(param);
		}
	}

	public List<Param> getParamList(String templateId) {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq("templateId", templateId), Param.class);
	}

	@Transactional
	public void del(String id) {
		sqlHelper.deleteById(id, Template.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("templateId", id), Param.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("templateValue", id), Param.class);
	}

	public Long getCountByName(String name) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("name", name), Template.class);
	}

	public Long getCountByNameWithOutId(String name, String id) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("name", name).ne("id", id), Template.class);
	}

}
