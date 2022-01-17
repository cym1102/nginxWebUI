package com.cym.controller.adminPage;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.ext.MonitorInfo;
import com.cym.ext.NetworkInfo;
import com.cym.service.MonitorService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.NetWorkUtil;

import cn.hutool.core.util.StrUtil;

@Mapping("/adminPage/monitor")
@Controller
public class MonitorController extends BaseController {
	@Inject
	MonitorService monitorService;
	@Inject
	SettingService settingService;
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView) {

		modelAndView.put("list", monitorService.getDiskInfo());

		String nginxPath = settingService.get("nginxPath");
		String nginxExe = settingService.get("nginxExe");
		String nginxDir = settingService.get("nginxDir");

		modelAndView.put("nginxDir", nginxDir);
		modelAndView.put("nginxExe", nginxExe);
		modelAndView.put("nginxPath", nginxPath);

		Boolean isInit = StrUtil.isNotEmpty(nginxExe);
		modelAndView.put("isInit", isInit.toString());

		modelAndView.view("/adminPage/monitor/index.html");
		return modelAndView;
	}

	@Mapping("check")
	public JsonResult check() {

		MonitorInfo monitorInfo = monitorService.getMonitorInfoOshi();

		return renderSuccess(monitorInfo);
	}

	@Mapping("network")
	public JsonResult network() {
		NetworkInfo networkInfo = NetWorkUtil.getNetworkDownUp();
		// System.err.println(JSONUtil.toJsonStr(networkInfo));
		return renderSuccess(networkInfo);
	}

	@Mapping("addNginxGiudeOver")
	public JsonResult addNginxGiudeOver(String nginxDir, String nginxExe) {

		settingService.set("nginxDir", nginxDir);
		settingService.set("nginxExe", nginxExe);
		return renderSuccess();
	}

}
