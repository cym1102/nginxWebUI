package com.cym.controller.adminPage;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.ext.TemplateExt;
import com.cym.model.Location;
import com.cym.model.Param;
import com.cym.model.Template;
import com.cym.service.TemplateService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Controller
@RequestMapping("/adminPage/template")
public class TemplateController extends BaseController {
	@Autowired
	TemplateService templateService;
	
	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) {
		List<Template> templateList = sqlHelper.findAll(Template.class);

		List<TemplateExt> extList = new ArrayList<>();
		for(Template template:templateList) {
			TemplateExt templateExt = new TemplateExt();
			templateExt.setTemplate(template);
			
			templateExt.setParamList(templateService.getParamList(template.getId()));
			templateExt.setCount(templateExt.getParamList().size()); 
			
			extList.add(templateExt);
		}
		
		modelAndView.addObject("templateList", extList);
		modelAndView.setViewName("/adminPage/template/index");
		return modelAndView;
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Template template,String paramJson) {
		
		if (StrUtil.isEmpty(template.getId())) {
			Long count = templateService.getCountByName(template.getName());
			if (count > 0) {
				return renderError(m.get("templateStr.sameName"));
			}
		}else {
			Long count = templateService.getCountByNameWithOutId(template.getName(), template.getId());
			if (count > 0) {
				return renderError(m.get("templateStr.sameName"));
			}
		}
		
		List<Param> params = JSONUtil.toList(JSONUtil.parseArray(paramJson), Param.class);
		
		templateService.addOver(template, params);

		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		Template template = sqlHelper.findById(id, Template.class);
		TemplateExt templateExt = new TemplateExt();
		templateExt.setTemplate(template);
		
		templateExt.setParamList(templateService.getParamList(template.getId()));
		templateExt.setCount(templateExt.getParamList().size()); 
		
		return renderSuccess(templateExt);
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {

		templateService.del(id);
		return renderSuccess();
	}
	
	@RequestMapping("getTemplate")
	@ResponseBody
	public JsonResult getTemplate() {

		return renderSuccess(sqlHelper.findAll(Template.class));
	}
	

}
