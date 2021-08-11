package com.cym.controller.adminPage;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.OperateLog;
import com.cym.service.OperateLogService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Page;

@Controller
@RequestMapping("/adminPage/operateLog")
public class OperateLogController extends BaseController{
	@Autowired
	OperateLogService operateLogService;
	
	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView, Page page) {
		page = operateLogService.search(page);
		
		modelAndView.addObject("page", page);
		
		modelAndView.setViewName("/adminPage/operatelog/index");
		return modelAndView;
	}
	
	
	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, OperateLog.class));
	}
}
