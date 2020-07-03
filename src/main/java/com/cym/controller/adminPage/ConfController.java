package com.cym.controller.adminPage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.VersionConfig;
import com.cym.controller.MainController;
import com.cym.ext.ConfExt;
import com.cym.ext.ConfFile;
import com.cym.service.ConfService;
import com.cym.service.ServerService;
import com.cym.service.SettingService;
import com.cym.service.UpstreamService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

@Controller
@RequestMapping("/adminPage/conf")
public class ConfController extends BaseController {
	final UpstreamController upstreamController;
	final UpstreamService upstreamService;
	final SettingService settingService;
	final ServerService serverService;
	final ConfService confService;
	final MainController mainController;

	@Autowired
	VersionConfig versionConfig;
	@Value("${project.version}")
	String currentVersion;

	public ConfController(UpstreamController upstreamController, UpstreamService upstreamService, SettingService settingService, ServerService serverService, ConfService confService,
			MainController mainController) {
		this.upstreamController = upstreamController;
		this.upstreamService = upstreamService;
		this.settingService = settingService;
		this.serverService = serverService;
		this.confService = confService;
		this.mainController = mainController;
	}

	@RequestMapping("")
	public ModelAndView index(ModelAndView modelAndView) {

		String nginxPath = settingService.get("nginxPath");
		modelAndView.addObject("nginxPath", nginxPath);

		String nginxExe = settingService.get("nginxExe");
		modelAndView.addObject("nginxExe", nginxExe);

		String nginxDir = settingService.get("nginxDir");
		modelAndView.addObject("nginxDir", nginxDir);

		String decompose = settingService.get("decompose");
		modelAndView.addObject("decompose", decompose);

		modelAndView.setViewName("/adminPage/conf/index");
		return modelAndView;
	}

	@RequestMapping(value = "nginxStatus")
	@ResponseBody
	public JsonResult nginxStatus() {
		boolean isRun = false;
		if (SystemTool.isWindows()) {
			String[] command = { "tasklist" };
			String rs = RuntimeUtil.execForStr(command);
			isRun = rs.toLowerCase().contains("nginx.exe");
		} else {
			String[] command = { "/bin/sh", "-c", "ps -ef|grep nginx" };
			String rs = RuntimeUtil.execForStr(command);
			isRun = rs.contains("nginx: master process") || rs.contains("nginx: worker process");
		}

		if (isRun) {
			return renderSuccess("nginx运行状态：<span class='green'>运行中</span>");
		} else {
			return renderSuccess("nginx运行状态：<span class='red'>未运行</span>");
		}

	}

	@RequestMapping(value = "replace")
	@ResponseBody
	public JsonResult replace(String json) {
		JSONObject jsonObject = JSONUtil.parseObj(json);

		String nginxPath = jsonObject.getStr("nginxPath");
		String nginxContent = jsonObject.getStr("nginxContent");
		List<String> subContent = jsonObject.getJSONArray("subContent").toList(String.class);
		List<String> subName = jsonObject.getJSONArray("subName").toList(String.class);

		if (nginxPath == null) {
			nginxPath = settingService.get("nginxPath");
		}
		if (!FileUtil.exist(nginxPath)) {
			return renderError("目标文件不存在");
		}

		try {
			confService.replace(nginxPath, nginxContent, subContent, subName);
			return renderSuccess("替换成功，原文件已备份");
		} catch (Exception e) {
			e.printStackTrace();

			return renderError("替换失败:" + e.getMessage());
		}

	}

	@RequestMapping(value = "check")
	@ResponseBody
	public JsonResult check(String nginxPath, String nginxExe, String nginxDir) {
		if (nginxPath == null) {
			nginxPath = settingService.get("nginxPath");
		}
		if (nginxExe == null) {
			nginxExe = settingService.get("nginxExe");
		}
		if (nginxDir == null) {
			nginxDir = settingService.get("nginxDir");
		}

		String rs;
		String cmd = null;
		try {
			if (SystemTool.isWindows()) {
				cmd = nginxExe + " -t -c " + nginxPath + " -p " + nginxDir;
			} else {
				cmd = nginxExe + " -t";
				if (nginxExe.contains("/")) {
					cmd = cmd + " -c " + nginxPath + " -p " + nginxDir;
				}
			}
			rs = RuntimeUtil.execForStr(cmd);
		} catch (Exception e) {
			e.printStackTrace();
			rs = e.getMessage().replace("\n", "<br>");
		}

		cmd = "<span class='blue'>" + cmd + "</span>";
		if (rs.contains("successful")) {
			return renderSuccess(cmd + "<br>效验成功<br>" + rs.replace("\n", "<br>"));
		} else {
			return renderError(cmd + "<br>效验失败<br>" + rs.replace("\n", "<br>"));
		}

	}

	@RequestMapping(value = "saveCmd")
	@ResponseBody
	public JsonResult saveCmd(String nginxPath, String nginxExe, String nginxDir) {
		nginxPath = nginxPath.replace("\\", "/");
		settingService.set("nginxPath", nginxPath);

		nginxExe = nginxExe.replace("\\", "/");
		settingService.set("nginxExe", nginxExe);

		nginxDir = nginxDir.replace("\\", "/");
		settingService.set("nginxDir", nginxDir);

		return renderSuccess();
	}

	@RequestMapping(value = "reload")
	@ResponseBody
	public synchronized JsonResult reload(String nginxPath, String nginxExe, String nginxDir) {
		if (nginxPath == null) {
			nginxPath = settingService.get("nginxPath");
		}
		if (nginxExe == null) {
			nginxExe = settingService.get("nginxExe");
		}
		if (nginxDir == null) {
			nginxDir = settingService.get("nginxDir");
		}

		try {
			String rs;
			String cmd;
			if (SystemTool.isWindows()) {
				cmd = nginxExe + " -s reload -c " + nginxPath + " -p " + nginxDir;
			} else {
				cmd = nginxExe + " -s reload";
				if (nginxExe.contains("/") && StrUtil.isNotEmpty(nginxPath) && StrUtil.isNotEmpty(nginxDir)) {
					cmd = cmd + " -c " + nginxPath + " -p " + nginxDir;
				}
			}
			rs = RuntimeUtil.execForStr(cmd);

			cmd = "<span class='blue'>" + cmd + "</span>";
			if (StrUtil.isEmpty(rs) || rs.contains("signal process started")) {
				return renderSuccess(cmd + "<br>重新装载成功<br>" + rs.replace("\n", "<br>"));
			} else {
				if (rs.contains("The system cannot find the file specified") || rs.contains("nginx.pid") || rs.contains("PID")) {
					rs = rs + "可能nginx进程没有启动";
				}

				return renderError(cmd + "<br>重新装载失败<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError("重新装载失败<br>" + e.getMessage().replace("\n", "<br>"));
		}
	}

	@RequestMapping(value = "start")
	@ResponseBody
	public JsonResult start(String nginxPath, String nginxExe, String nginxDir) {
		if (nginxPath == null) {
			nginxPath = settingService.get("nginxPath");
		}
		if (nginxExe == null) {
			nginxExe = settingService.get("nginxExe");
		}
		if (nginxDir == null) {
			nginxDir = settingService.get("nginxDir");
		}
		try {
			String rs = "";
			String cmd;
			if (SystemTool.isWindows()) {
				cmd = "cmd /c start nginx.exe" + " -c " + nginxPath + " -p " + nginxDir;
				RuntimeUtil.exec(new String[] {}, new File(nginxDir), cmd);
			} else {
				cmd = nginxExe;
				if (nginxExe.contains("/") && StrUtil.isNotEmpty(nginxPath) && StrUtil.isNotEmpty(nginxDir)) {
					cmd = cmd + " -c " + nginxPath + " -p " + nginxDir;
				}
				rs = RuntimeUtil.execForStr(cmd);
			}

			cmd = "<span class='blue'>" + cmd + "</span>";
			if (StrUtil.isEmpty(rs) || rs.contains("signal process started")) {
				return renderSuccess(cmd + "<br>启动成功<br>" + rs.replace("\n", "<br>"));
			} else {
				return renderError(cmd + "<br>启动失败<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError("启动失败");
		}
	}

	@RequestMapping(value = "stop")
	@ResponseBody
	public JsonResult stop(String nginxExe, String nginxDir) {
		if (nginxExe == null) {
			nginxExe = settingService.get("nginxExe");
		}
		if (nginxDir == null) {
			nginxDir = settingService.get("nginxDir");
		}
		try {
			String rs;
			String cmd;
			if (SystemTool.isWindows()) {
				cmd = "taskkill /im nginx.exe /f";
			} else {
				cmd = nginxExe + " -s stop";
				if (nginxExe.contains("/") && StrUtil.isNotEmpty(nginxDir)) {
					cmd = cmd + " -p " + nginxDir;
				}
			}
			rs = RuntimeUtil.execForStr(cmd);

			cmd = "<span class='blue'>" + cmd + "</span>";
			if (StrUtil.isEmpty(rs) || rs.contains("已终止进程")) {
				return renderSuccess(cmd + "<br>停止成功<br>" + rs.replace("\n", "<br>"));
			} else {
				return renderError(cmd + "<br>停止失败<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError("停止失败<br>" + e.getMessage().replace("\n", "<br>"));
		}
	}

	@RequestMapping(value = "loadConf")
	@ResponseBody
	public JsonResult loadConf() {
		String decompose = settingService.get("decompose");

		ConfExt confExt = confService.buildConf(StrUtil.isNotEmpty(decompose) && decompose.equals("true"));
		return renderSuccess(confExt);
	}

	@RequestMapping(value = "loadOrg")
	@ResponseBody
	public JsonResult loadOrg(String nginxPath) {

		String decompose = settingService.get("decompose");
		ConfExt confExt = confService.buildConf(StrUtil.isNotEmpty(decompose) && decompose.equals("true"));

		if (StrUtil.isNotEmpty(nginxPath) && FileUtil.exist(nginxPath)) {
			String orgStr = FileUtil.readString(nginxPath, StandardCharsets.UTF_8);
			confExt.setConf(orgStr);

			for (ConfFile confFile : confExt.getFileList()) {
				confFile.setConf("");

				String filePath = nginxPath.replace("nginx.conf", "conf.d/" + confFile.getName());
				if (FileUtil.exist(filePath)) {
					confFile.setConf(FileUtil.readString(filePath, StandardCharsets.UTF_8));
				}
			}

			return renderSuccess(confExt);
		} else {
			return renderError("文件不存在");
		}

	}

	@RequestMapping(value = "decompose")
	@ResponseBody
	public JsonResult decompose(String decompose) {
		settingService.set("decompose", decompose);
		return renderSuccess();
	}

	@RequestMapping(value = "update")
	@ResponseBody
	public JsonResult update() {
		versionConfig.getNewVersion();
		if (Integer.parseInt(currentVersion.replace(".", "").replace("v", "")) < Integer.parseInt(versionConfig.getVersion().getVersion().replace(".", "").replace("v", ""))) {
			mainController.autoUpdate(versionConfig.getVersion().getUrl());
			return renderSuccess("更新成功");
		} else {
			return renderSuccess("无需更新");
		}
	}

}
