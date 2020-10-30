package com.cym.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Param;
import com.cym.model.Template;

import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Service
public class ParamService {

	@Autowired
	SqlHelper sqlHelper;

	public String getJsonByTypeId(String id, String type) {
		List<Param> list = sqlHelper.findListByQuery(new ConditionAndWrapper().eq(type + "Id", id), Param.class);
		for (Param param : list) {
			if (StrUtil.isNotEmpty(param.getTemplateValue())) {
				Template template = sqlHelper.findById(param.getTemplateValue(), Template.class);
				param.setTemplateName(template.getName());
			}

		}
		return JSONUtil.toJsonStr(list);
	}

	public List<Param> getListByTypeId(String id, String type) {
		List<Param> list = new ArrayList<>();
		// 默认配置的参数
		List<Template> templateList = sqlHelper.findListByQuery(new ConditionAndWrapper().eq(Template::getDef, type), Template.class);
		for (Template template : templateList) {
			List<Param> addList = sqlHelper.findListByQuery(new ConditionAndWrapper().eq(Param::getTemplateId, template.getId()), Param.class);
			list.addAll(addList);
		}

		list.addAll(sqlHelper.findListByQuery(new ConditionAndWrapper().eq(type + "Id", id), Param.class));

		return list;
	}

}
