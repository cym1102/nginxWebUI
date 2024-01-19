package com.cym.controller.adminPage;

import java.util.Date;

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
import com.cym.utils.SystemTool;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RuntimeUtil;
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

		boolean isInit = StrUtil.isNotEmpty(nginxExe);
		modelAndView.put("isInit", Boolean.toString(isInit));

		modelAndView.view("/adminPage/monitor/index.html");
		return modelAndView;
	}

	@Mapping("load")
	public JsonResult load() {

		MonitorInfo monitorInfo = monitorService.getMonitorInfoOshi();

		return renderSuccess(monitorInfo);
	}

	@Mapping("network")
	public JsonResult network() throws InterruptedException {
		if (SystemTool.isLinux() && !hasIfconfig()) {
			Thread.sleep(2000);
			NetworkInfo networkInfo = new NetworkInfo();
			networkInfo.setTime(DateUtil.format(new Date(), "HH:mm:ss"));
			networkInfo.setSend(0d);
			networkInfo.setReceive(0d);

			return renderSuccess(networkInfo);
		}

		NetworkInfo networkInfo = NetWorkUtil.getNetworkDownUp();
		return renderSuccess(networkInfo);
	}

	private boolean hasIfconfig() {
		String rs = RuntimeUtil.execForStr("which ifconfig");
		if (StrUtil.isNotEmpty(rs)) {
			return true;
		}

		return false;
	}

	@Mapping("addNginxGiudeOver")
	public JsonResult addNginxGiudeOver(String nginxDir, String nginxExe) {

		settingService.set("nginxDir", nginxDir);
		settingService.set("nginxExe", nginxExe);
		return renderSuccess();
	}

}
