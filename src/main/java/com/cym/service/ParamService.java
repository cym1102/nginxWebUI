package com.cym.service;

import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import com.cym.model.Param;
import com.cym.model.Template;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Component
public class ParamService {

	@Inject
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

		if (type.contains("server")) {
			type = "server";
		}

		list.addAll(sqlHelper.findListByQuery(new ConditionAndWrapper().eq(type + "Id", id), Param.class));

		return list;
	}

	public List<Param> getList(String serverId, String locationId, String upstreamId) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper();
		if (StrUtil.isNotEmpty(serverId)) {
			conditionAndWrapper.eq("serverId", serverId);
		}
		if (StrUtil.isNotEmpty(locationId)) {
			conditionAndWrapper.eq("locationId", locationId);
		}
		if (StrUtil.isNotEmpty(upstreamId)) {
			conditionAndWrapper.eq("upstreamId", upstreamId);
		}

		return sqlHelper.findListByQuery(conditionAndWrapper, Param.class);
	}

}
