package com.cym.controller.adminPage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.MonitorInfo;
import com.cym.service.MonitorService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.util.NumberUtil;

@RequestMapping("/adminPage/monitor")
@Controller
public class MonitorController extends BaseController {
	@Autowired
	MonitorService monitorService;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) {
		File[] roots = File.listRoots();// 获取磁盘分区列表
		List<Map<String, String>> list = new ArrayList<>() ;
		for (File file : roots) {
			Map<String, String> map = new HashMap<String, String>();

			long freeSpace = file.getFreeSpace();
			long totalSpace = file.getTotalSpace();
			long usableSpace = totalSpace - freeSpace;

			map.put("path", file.getPath());
			map.put("freeSpace", freeSpace / 1024 / 1024 / 1024 + "G");// 空闲空间
			map.put("usableSpace", usableSpace / 1024 / 1024 / 1024 + "G");// 已用空间
			map.put("totalSpace", totalSpace / 1024 / 1024 / 1024 + "G");// 总空间
			map.put("percent", NumberUtil.decimalFormat("#.##%", (double) usableSpace / (double) totalSpace));// 总空间

			list.add(map);
		}

		modelAndView.addObject("list", list);
		modelAndView.setViewName("/adminPage/monitor/index");
		return modelAndView;
	}

	@RequestMapping("check")
	@ResponseBody
	public JsonResult check() {

		MonitorInfo monitorInfo = monitorService.getMonitorInfo();
		return renderSuccess(monitorInfo);
	}
	

}
