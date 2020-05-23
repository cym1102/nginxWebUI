package com.cym.controller.adminPage;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.Http;
import com.cym.service.HttpService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Controller
@RequestMapping("/adminPage/http")
public class HttpController extends BaseController {
	@Autowired
	HttpService httpService;
	
	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) {
		List<Http> httpList = sqlHelper.findAll(new Sort("name", Direction.ASC), Http.class);

		modelAndView.addObject("httpList", httpList);
		modelAndView.setViewName("/adminPage/http/index");
		return modelAndView;
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Http http)  {
		if(StrUtil.isEmpty(http.getId()) && httpService.hasName(http.getName())) {
			return renderError("名称已存在");
		}
		sqlHelper.insertOrUpdate(http);

		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id)  {
		return renderSuccess(sqlHelper.findById(id, Http.class));
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id)  {
		sqlHelper.deleteById(id, Http.class);
		
		return renderSuccess();
	}

	
	@RequestMapping("addGiudeOver")
	@ResponseBody
	public JsonResult addGiudeOver(String json)  {
		List<Http> https = JSONUtil.toList(JSONUtil.parseArray(json), Http.class); 
		
		httpService.setAll(https);
		
		return renderSuccess();
	}

	
	
}
