package com.cym.controller.adminPage;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.ScheduleTask;
import com.cym.model.Log;
import com.cym.service.LogService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Page;

@Controller
@RequestMapping("/adminPage/log")
public class LogController extends BaseController {
	@Autowired
	SettingService settingService;
	@Autowired
	LogService logService;
	@Autowired
	ScheduleTask scheduleTask;
	
	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView, Page page) {
		page = logService.search(page);

		modelAndView.addObject("page", page);
		modelAndView.setViewName("/adminPage/log/index");
		return modelAndView;
	}
	
	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		sqlHelper.deleteById(id, Log.class);
		return renderSuccess();
	}
	
	@RequestMapping("delAll")
	@ResponseBody
	public JsonResult delAll(String id) {
		sqlHelper.deleteByQuery(null, Log.class); 
		return renderSuccess();
	}
	
	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		Log log = sqlHelper.findById(id, Log.class);
		return renderSuccess(log);
		
	}
	
	
	@RequestMapping("analysis")
	@ResponseBody
	public JsonResult analysis() {
		scheduleTask.diviLog();
		return renderSuccess();
		
	}
	

}
