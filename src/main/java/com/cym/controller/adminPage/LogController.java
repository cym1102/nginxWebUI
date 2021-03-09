package com.cym.controller.adminPage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
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
import com.cym.utils.SystemTool;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.URLUtil;

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
		
		modelAndView.addObject("isLinux", SystemTool.isLinux());
		modelAndView.setViewName("/adminPage/log/index");
		return modelAndView;
	}
	
	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Log log) {
		if (logService.hasDir(log.getPath(),log.getId())) {
			return renderError(m.get("logStr.sameDir"));
		}
		
		if(FileUtil.isDirectory(log.getPath())) {
			return renderError(m.get("logStr.notFile"));
		}
		
		sqlHelper.insertOrUpdate(log);
		return renderSuccess();
	}
	
	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, Log.class));
	}
	
	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		sqlHelper.deleteById(id, Log.class);
		return renderSuccess();
	}

	@RequestMapping("tail")
	public ModelAndView tail(ModelAndView modelAndView, String id) {
		modelAndView.addObject("id", id);
		modelAndView.setViewName("/adminPage/log/tail");
		return modelAndView;
	}
	
	
	@ResponseBody
	@RequestMapping("down")
	public void down(ModelAndView modelAndView, String id, HttpServletResponse response) {
		Log log = sqlHelper.findById(id, Log.class);
		outputStream(new File(log.getPath()), response);  
	}

	
	private void outputStream(File file, HttpServletResponse response) {
		try {
			response.setContentType("application/octet-stream");
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=" + URLUtil.encode(file.getName());
			response.setHeader(headerKey, headerValue);

			InputStream inputStream = new FileInputStream(file);
			IOUtils.copy(inputStream, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
