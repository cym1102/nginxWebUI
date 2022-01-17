package com.cym.service;

import java.util.Collections;
import java.util.List;

import org.noear.solon.annotation.Inject;
import org.noear.solon.extend.aspect.annotation.Service;

import com.cym.model.Param;
import com.cym.model.Template;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

@Service
public class TemplateService {
	@Inject
	SqlHelper sqlHelper;

	
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
