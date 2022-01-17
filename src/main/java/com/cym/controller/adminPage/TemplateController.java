package com.cym.controller.adminPage;

import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;

import com.cym.ext.TemplateExt;
import com.cym.model.Param;
import com.cym.model.Template;
import com.cym.service.TemplateService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Controller
@Mapping("/adminPage/template")
public class TemplateController extends BaseController {
	@Inject
	TemplateService templateService;
	
	@Mapping("")
	public ModelAndView index( ModelAndView modelAndView) {
		List<Template> templateList = sqlHelper.findAll(Template.class);

		List<TemplateExt> extList = new ArrayList<>();
		for(Template template:templateList) {
			TemplateExt templateExt = new TemplateExt();
			templateExt.setTemplate(template);
			
			templateExt.setParamList(templateService.getParamList(template.getId()));
			templateExt.setCount(templateExt.getParamList().size()); 
			
			extList.add(templateExt);
		}
		
		modelAndView.put("templateList", extList);
		modelAndView.view("/adminPage/template/index.html");
		return modelAndView;
	}

	@Mapping("addOver")
	public JsonResult addOver(Template template,String paramJson) {
		
		if (StrUtil.isEmpty(template.getId())) {
			Long count = templateService.getCountByName(template.getName());
			if (count > 0) {
				return renderError(m.get("templateStr.sameName"));
			}
		} else {
			Long count = templateService.getCountByNameWithOutId(template.getName(), template.getId());
			if (count > 0) {
				return renderError(m.get("templateStr.sameName"));
			}
		}
		
		List<Param> params = JSONUtil.toList(JSONUtil.parseArray(paramJson), Param.class);
		
		templateService.addOver(template, params);

		return renderSuccess();
	}

	@Mapping("detail")
	public JsonResult detail(String id) {
		Template template = sqlHelper.findById(id, Template.class);
		TemplateExt templateExt = new TemplateExt();
		templateExt.setTemplate(template);
		
		templateExt.setParamList(templateService.getParamList(template.getId()));
		templateExt.setCount(templateExt.getParamList().size()); 
		
		return renderSuccess(templateExt);
	}

	@Mapping("del")
	public JsonResult del(String id) {

		templateService.del(id);
		return renderSuccess();
	}
	
	@Mapping("getTemplate")
	public JsonResult getTemplate() {

		return renderSuccess(sqlHelper.findAll(Template.class));
	}
	

}
