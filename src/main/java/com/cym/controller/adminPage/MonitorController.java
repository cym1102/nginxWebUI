package com.cym.controller.adminPage;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.ext.MonitorInfo;
import com.cym.ext.NetworkInfo;
import com.cym.service.MonitorService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.NetWorkUtil;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@RequestMapping("/adminPage/monitor")
@Controller
public class MonitorController extends BaseController {
	@Autowired
	MonitorService monitorService;
	@Autowired
	SettingService settingService;
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) {

		modelAndView.addObject("list", monitorService.getDiskInfo());

		String nginxPath = settingService.get("nginxPath");
		String nginxExe = settingService.get("nginxExe");
		String nginxDir = settingService.get("nginxDir");

		modelAndView.addObject("nginxDir", nginxDir);
		modelAndView.addObject("nginxExe", nginxExe);
		modelAndView.addObject("nginxPath", nginxPath);

		Boolean isInit = StrUtil.isNotEmpty(nginxExe);
		modelAndView.addObject("isInit", isInit.toString());

		modelAndView.setViewName("/adminPage/monitor/index");
		return modelAndView;
	}

	@RequestMapping("check")
	@ResponseBody
	public JsonResult check() {

		MonitorInfo monitorInfo = monitorService.getMonitorInfoOshi();

		return renderSuccess(monitorInfo);
	}
	
	@RequestMapping("network")
	@ResponseBody
	public JsonResult network() {
		NetworkInfo networkInfo = NetWorkUtil.getNetworkDownUp();
		//System.err.println(JSONUtil.toJsonStr(networkInfo));
		return renderSuccess(networkInfo);
	}

	@RequestMapping("addNginxGiudeOver")
	@ResponseBody
	public JsonResult addNginxGiudeOver(String nginxDir, String nginxExe) {

		settingService.set("nginxDir", nginxDir);
		settingService.set("nginxExe", nginxExe);
		return renderSuccess();
	}

}
