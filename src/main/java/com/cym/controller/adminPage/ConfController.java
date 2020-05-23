package com.cym.controller.adminPage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.SQLException;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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

@Controller
@RequestMapping("/adminPage/conf")
public class ConfController extends BaseController {
	@Autowired
	UpstreamController upstreamController;
	@Autowired
	UpstreamService upstreamService;
	@Autowired
	SettingService settingService;
	@Autowired
	ServerService serverService;
	@Autowired
	ConfService confService;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) throws IOException, SQLException {

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

	@RequestMapping(value = "replace")
	@ResponseBody
	public JsonResult replace(String nginxPath, String nginxContent, String[] subContent, String[] subName) {
		nginxPath = nginxPath.replace("\\", "/");
		settingService.set("nginxPath", nginxPath);

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
		nginxPath = nginxPath.replace("\\", "/");
		nginxExe = nginxExe.replace("\\", "/");
		nginxDir = nginxDir.replace("\\", "/");

		settingService.set("nginxPath", nginxPath);
		settingService.set("nginxExe", nginxExe);
		settingService.set("nginxDir", nginxDir);
		try {
			String rs = null;
			String cmd = null;
			if (SystemTool.isWindows()) {
				cmd = nginxExe + " -t -c " + nginxPath + " -p " + nginxDir;
				rs = RuntimeUtil.execForStr(cmd);
			} else {
				cmd = nginxExe + " -t";
				if (nginxExe.contains("/")) {
					cmd = cmd + " -c " + nginxPath + " -p " + nginxDir;
				}
				rs = RuntimeUtil.execForStr(cmd);
			}

			cmd = "<span class='blue'>" + cmd + "</span>";
			if (rs.contains("successful")) {
				return renderSuccess(cmd + "<br>效验成功");
			} else {
				return renderError(cmd + "<br>效验失败:<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError("效验失败:<br>" + e.getMessage().replace("\n", "<br>"));
		}
	}

	@RequestMapping(value = "reload")
	@ResponseBody
	public JsonResult reload(String nginxPath, String nginxExe, String nginxDir) {
		nginxPath = nginxPath.replace("\\", "/");
		nginxExe = nginxExe.replace("\\", "/");
		nginxDir = nginxDir.replace("\\", "/");

		settingService.set("nginxPath", nginxPath);
		settingService.set("nginxExe", nginxExe);
		settingService.set("nginxDir", nginxDir);

		try {
			String rs = null;
			String cmd = null;
			if (SystemTool.isWindows()) {
				cmd = nginxExe + " -s reload -c " + nginxPath + " -p " + nginxDir;
				rs = RuntimeUtil.execForStr(cmd);
			} else {
				cmd = nginxExe + " -s reload";
				if (nginxExe.contains("/")) {
					cmd = cmd + " -c " + nginxPath + " -p " + nginxDir;
				}
				rs = RuntimeUtil.execForStr(cmd);
			}

			cmd = "<span class='blue'>" + cmd + "</span>";
			if (StrUtil.isEmpty(rs)) {
				return renderSuccess(cmd + "<br>重新装载成功");
			} else {
				if (rs.contains("The system cannot find the file specified") || rs.contains("nginx.pid")) {
					rs = rs.replace("\n", "<br>") + "可能nginx进程没有启动";
				}

				return renderError(cmd + "<br>重新装载失败:<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError("重新装载失败:<br>" + e.getMessage().replace("\n", "<br>"));
		}
	}

	@RequestMapping(value = "start")
	@ResponseBody
	public JsonResult start(String nginxExe, String nginxDir) {
		nginxExe = nginxExe.replace("\\", "/");
		nginxDir = nginxDir.replace("\\", "/");

		settingService.set("nginxExe", nginxExe);
		settingService.set("nginxDir", nginxDir);

		try {
			String rs = null;
			String cmd = null;
			if (SystemTool.isWindows()) {
				cmd = "cmd /c start nginx.exe";
				RuntimeUtil.exec(new String[] {}, new File(nginxDir), cmd);
			} else {
				cmd = nginxExe;
				if (nginxExe.contains("/")) {
					cmd = cmd + " -p " + nginxDir;
				}
				rs = RuntimeUtil.execForStr(cmd);
			}

			if (StrUtil.isEmpty(rs)) {
				return renderSuccess(cmd + "<br>启动成功");
			} else {
				return renderError(cmd + "<br>启动失败:<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError("启动失败:<br>" + e.getMessage().replace("\n", "<br>"));
		}
	}

	@RequestMapping(value = "stop")
	@ResponseBody
	public JsonResult stop(String nginxExe, String nginxDir) {
		nginxExe = nginxExe.replace("\\", "/");
		nginxDir = nginxDir.replace("\\", "/");

		settingService.set("nginxExe", nginxExe);
		settingService.set("nginxDir", nginxDir);

		try {
			String rs = null;
			String cmd = null;
			if (SystemTool.isWindows()) {
				cmd = nginxExe + " -s stop -p " + nginxDir;
				rs = RuntimeUtil.execForStr(cmd);
			} else {
				cmd = nginxExe + " -s stop";
				if (nginxExe.contains("/")) {
					cmd = cmd + " -p " + nginxDir;
				}
				rs = RuntimeUtil.execForStr(cmd);
			}

			if (StrUtil.isEmpty(rs)) {
				return renderSuccess(cmd + "<br>停止成功");
			} else {
				return renderError(cmd + "<br>停止失败:<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError("停止失败:<br>" + e.getMessage().replace("\n", "<br>"));
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
		nginxPath = nginxPath.replace("\\", "/");

		settingService.set("nginxPath", nginxPath);

		String decompose = settingService.get("decompose");
		ConfExt confExt = confService.buildConf(StrUtil.isNotEmpty(decompose) && decompose.equals("true"));

		if (FileUtil.exist(nginxPath)) {
			String orgStr = FileUtil.readString(nginxPath, Charset.defaultCharset());
			confExt.setConf(orgStr);

			for (ConfFile confFile : confExt.getFileList()) {
				confFile.setConf("");

				String filePath = nginxPath.replace("nginx.conf", "conf.d/" + confFile.getName());
				if (FileUtil.exist(filePath)) {
					confFile.setConf(FileUtil.readString(filePath, Charset.defaultCharset()));
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

}
