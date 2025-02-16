package com.cym.controller.adminPage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.VersionConfig;
import com.cym.ext.ConfExt;
import com.cym.ext.ConfFile;
import com.cym.model.Admin;
import com.cym.service.ConfService;
import com.cym.service.ServerService;
import com.cym.service.SettingService;
import com.cym.service.UpstreamService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.NginxUtils;
import com.cym.utils.SystemTool;
import com.cym.utils.ToolUtils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.PathUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

@Controller
@Mapping("/adminPage/conf")
public class ConfController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	UpstreamService upstreamService;
	@Inject
	SettingService settingService;
	@Inject
	ServerService serverService;
	@Inject
	ConfService confService;
	@Inject
	MainController mainController;
	@Inject
	VersionConfig versionConfig;

	String aesKey = "aes";

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView) {
		// 获取并过滤数据库中的路径
		String nginxPath = ToolUtils.handleConf(settingService.get("nginxPath"));
		settingService.set("nginxPath", nginxPath);
		modelAndView.put("nginxPath", nginxPath);

		String nginxExe = ToolUtils.handleConf(settingService.get("nginxExe"));
		settingService.set("nginxExe", nginxExe);
		modelAndView.put("nginxExe", nginxExe);

		String nginxDir = ToolUtils.handleConf(settingService.get("nginxDir"));
		settingService.set("nginxDir", nginxDir);
		modelAndView.put("nginxDir", nginxDir);

		String decompose = settingService.get("decompose");
		modelAndView.put("decompose", decompose);

		modelAndView.put("tmp", homeConfig.home + "temp/nginx.conf");

		modelAndView.view("/adminPage/conf/index.html");
		return modelAndView;
	}

	@Mapping(value = "nginxStatus")
	public JsonResult nginxStatus() {
		if (NginxUtils.isRun()) {
			return renderSuccess(m.get("confStr.nginxStatus") + "：<span class='green'>" + m.get("confStr.running") + "</span>");
		} else {
			return renderSuccess(m.get("confStr.nginxStatus") + "：<span class='red'>" + m.get("confStr.stopped") + "</span>");
		}

	}

	@Mapping(value = "replace")
	public JsonResult replace(String json, String adminName) {

		if (StrUtil.isEmpty(json)) {
			json = getReplaceJson();
		}

		JSONObject jsonObject = JSONUtil.parseObj(json);

		String nginxPath = jsonObject.getStr("nginxPath");
		String nginxContent = Base64.decodeStr(jsonObject.getStr("nginxContent"));

		List<String> subContent = jsonObject.getJSONArray("subContent").toList(String.class);
		for (int i = 0; i < subContent.size(); i++) {
			String content = Base64.decodeStr(subContent.get(i));
			subContent.set(i, content);
		}
		List<String> subName = jsonObject.getJSONArray("subName").toList(String.class);

		if (nginxPath == null) {
			nginxPath = settingService.get("nginxPath");
		}

		if (FileUtil.isDirectory(nginxPath)) {
			// 是文件夹, 提示
			return renderError(m.get("confStr.error2"));
		}

		try {
			if (StrUtil.isEmpty(adminName)) {
				Admin admin = getAdmin();
				adminName = admin.getName();
			}

			confService.replace(nginxPath, nginxContent, subContent, subName, true, adminName);
			return renderSuccess(m.get("confStr.replaceSuccess"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return renderError(m.get("confStr.error3") + ":" + e.getMessage());
		}

	}

	public String getReplaceJson() {
		String decompose = settingService.get("decompose");
		ConfExt confExt = confService.buildConf(StrUtil.isNotEmpty(decompose) && decompose.equals("true"), false);

		JSONObject jsonObject = new JSONObject();
		jsonObject.set("nginxContent", Base64.encode(confExt.getConf()));
		jsonObject.set("subContent", new JSONArray());
		jsonObject.set("subName", new JSONArray());
		for (ConfFile confFile : confExt.getFileList()) {
			jsonObject.getJSONArray("subContent").add(Base64.encode(confFile.getConf()));
			jsonObject.getJSONArray("subName").add(confFile.getName());
		}
		return jsonObject.toStringPretty();
	}

	/**
	 * 检查数据库内部配置
	 * 
	 * @param nginxPath
	 * @param nginxExe
	 * @param nginxDir
	 * @return
	 */
	@Mapping(value = "checkBase")
	public JsonResult checkBase() {
		String nginxExe = ToolUtils.handleConf(settingService.get("nginxExe"));
		settingService.set("nginxExe", nginxExe);
		String nginxDir = ToolUtils.handleConf(settingService.get("nginxDir"));
		settingService.set("nginxDir", nginxDir);

		String rs = null;
		String cmd = null;

		FileUtil.del(homeConfig.home + "temp");
		String fileTemp = homeConfig.home + "temp/nginx.conf";

		ConfExt confExt = confService.buildConf(false, true);

		String nginxContent = confExt.getConf();
		List<String> subContent = confExt.getFileList().stream().map(ConfFile::getConf).collect(Collectors.toList());
		List<String> subName = confExt.getFileList().stream().map(ConfFile::getName).collect(Collectors.toList());

		confService.replace(fileTemp, nginxContent, subContent, subName, false, null);

		try {
			cmd = nginxExe + " -t -c " + fileTemp;
			if (StrUtil.isNotEmpty(nginxDir)) {
				cmd += " -p " + nginxDir;
			}
			rs = RuntimeUtil.execForStr(cmd);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			rs = e.getMessage().replace("\n", "<br>");
		}

		cmd = "<span class='blue'>" + cmd + "</span>";
		if (rs.contains("successful")) {
			return renderSuccess(cmd + "<br>" + m.get("confStr.verifySuccess") + "<br>" + rs.replace("\n", "<br>"));
		} else {
			return renderError(cmd + "<br>" + m.get("confStr.verifyFail") + "<br>" + rs.replace("\n", "<br>"));
		}

	}

	/**
	 * 检查页面上的配置
	 * 
	 * @param nginxPath
	 * @param nginxExe
	 * @param nginxDir
	 * @param json
	 * @return
	 */
	@Mapping(value = "check")
	public JsonResult check(String nginxPath, String nginxExe, String nginxDir, String json) {
		if (nginxExe == null) {
			nginxExe = ToolUtils.handleConf(settingService.get("nginxExe"));
			settingService.set("nginxExe", nginxExe);
		}
		if (nginxDir == null) {
			nginxDir = ToolUtils.handleConf(settingService.get("nginxDir"));
			settingService.set("nginxDir", nginxDir);
		}

		JSONObject jsonObject = JSONUtil.parseObj(json);
		String nginxContent = Base64.decodeStr(jsonObject.getStr("nginxContent"), CharsetUtil.CHARSET_UTF_8);
		List<String> subContent = jsonObject.getJSONArray("subContent").toList(String.class);
		List<String> subName = jsonObject.getJSONArray("subName").toList(String.class);

		for (int i = 0; i < subContent.size(); i++) { // 解码
			String content = Base64.decodeStr(subContent.get(i), CharsetUtil.CHARSET_UTF_8);
			subContent.set(i, content);
		}

		FileUtil.del(homeConfig.home + "temp");
		String fileTemp = homeConfig.home + "temp/nginx.conf";

		confService.replace(fileTemp, nginxContent, subContent, subName, false, null);

		String rs = null;
		String cmd = null;

		try {
			cmd = nginxExe + " -t -c " + fileTemp;
			if (StrUtil.isNotEmpty(nginxDir)) {
				cmd += " -p " + nginxDir;
			}
			rs = RuntimeUtil.execForStr(cmd);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			rs = e.getMessage().replace("\n", "<br>");
		}

		cmd = "<span class='blue'>" + cmd + "</span>";
		if (rs.contains("test is successful")) {
			return renderSuccess(cmd + "<br>" + m.get("confStr.verifySuccess") + "<br>" + rs.replace("\n", "<br>"));
		} else {
			return renderSuccess(cmd + "<br>" + m.get("confStr.verifyFail") + "<br>" + rs.replace("\n", "<br>"));
		}

	}

	@Mapping(value = "saveCmd")
	public JsonResult saveCmd(String nginxPath, String nginxExe, String nginxDir) {
		nginxPath = ToolUtils.handlePath(nginxPath);
		nginxExe = ToolUtils.handlePath(nginxExe);
		nginxDir = ToolUtils.handlePath(nginxDir);

		if (StrUtil.isNotEmpty(nginxPath) && !isFile(nginxPath)) {
			nginxPath = null;
		}
		if (StrUtil.isNotEmpty(nginxDir) && !isFile(nginxDir)) {
			nginxDir = null;
		}
		if (StrUtil.isNotEmpty(nginxExe) && !isFile(nginxExe) && !isSafeCmd(nginxExe)) {
			nginxExe = null;
		}
		if (StrUtil.isNotEmpty(nginxExe) && isFile(nginxExe) && !isSafeEnd(nginxExe)) {
			nginxExe = null;
		}

		if (nginxPath != null) {
			settingService.set("nginxPath", nginxPath);
			System.out.println("nginxPath -> " + nginxPath);
		}
		if (nginxExe != null) {
			settingService.set("nginxExe", nginxExe);
			System.out.println("nginxExe -> " + nginxExe);
		}
		if (nginxDir != null) {
			settingService.set("nginxDir", nginxDir);
			System.out.println("nginxDir -> " + nginxDir);
		}

		Map<String, String> map = new HashMap<>();
		map.put("nginxPath", nginxPath);
		map.put("nginxExe", nginxExe);
		map.put("nginxDir", nginxDir);
		System.out.println("");
		return renderSuccess(map);
	}

	private boolean isSafeEnd(String nginxExe) {
		return nginxExe.endsWith("nginx") //
				|| nginxExe.endsWith("openresty") //
				|| nginxExe.endsWith("nginx.exe") //
				|| nginxExe.endsWith("openrestys.exe");
	}

	private boolean isSafeCmd(String nginxExe) {
		return nginxExe.equals("nginx") //
				|| nginxExe.equals("openresty") //
				|| nginxExe.equals("nginx.exe") //
				|| nginxExe.equals("openrestys.exe");
	}

	private boolean isFile(String path) {
		return FileUtil.isDirectory(path) || FileUtil.isFile(path);
	}

	@Mapping(value = "reload")
	public synchronized JsonResult reload(String nginxPath, String nginxExe, String nginxDir) {
		if (nginxPath == null) {
			nginxPath = ToolUtils.handlePath(settingService.get("nginxPath"));
			settingService.set("nginxPath", nginxPath);
		}
		if (nginxExe == null) {
			nginxExe = ToolUtils.handlePath(settingService.get("nginxExe"));
			settingService.set("nginxExe", nginxExe);
		}
		if (nginxDir == null) {
			nginxDir = ToolUtils.handlePath(settingService.get("nginxDir"));
			settingService.set("nginxDir", nginxDir);
		}

		try {
			String cmd = nginxExe + " -s reload -c " + nginxPath;
			if (StrUtil.isNotEmpty(nginxDir)) {
				cmd += " -p " + nginxDir;
			}
			String rs = RuntimeUtil.execForStr(cmd);

			cmd = "<span class='blue'>" + cmd + "</span>";
			if (!rs.contains("[error]") && !rs.contains("[emerg]")) {
				return renderSuccess(cmd + "<br>" + m.get("confStr.reloadSuccess") + "<br>" + rs.replace("\n", "<br>"));
			} else {
				if (rs.contains("The system cannot find the file specified") || rs.contains("nginx.pid") || rs.contains("PID")) {
					rs = rs + m.get("confStr.mayNotRun");
				}

				return renderSuccess(cmd + "<br>" + m.get("confStr.reloadFail") + "<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return renderSuccess(m.get("confStr.reloadFail") + "<br>" + e.getMessage().replace("\n", "<br>"));
		}
	}

	@Mapping(value = "runCmd")
	public JsonResult runCmd(String cmd, String type) {

		if (StrUtil.isNotEmpty(type)) {
			settingService.set(type, cmd);
		}

		// 仅执行nginx相关的命令，而不是其他的恶意命令
		cmd = buildRealCmd(cmd);
		if (StrUtil.isEmpty(cmd)) {
			return renderSuccess(m.get("confStr.notAvailableCmd"));
		}

		try {
			String rs = "";
			if (SystemTool.isWindows()) {
				RuntimeUtil.exec("cmd /c start " + cmd);
			} else {
				rs = RuntimeUtil.execForStr("/bin/sh", "-c", cmd);
			}

			cmd = "<span class='blue'>" + cmd + "</span>";
			if (StrUtil.isEmpty(rs) || rs.contains("已终止进程") //
					|| rs.contains("signal process started") //
					|| rs.toLowerCase().contains("terminated process") //
					|| rs.toLowerCase().contains("starting") //
					|| rs.toLowerCase().contains("stopping")) {
				return renderSuccess(cmd + "<br>" + m.get("confStr.runSuccess") + "<br>" + rs.replace("\n", "<br>"));
			} else {
				return renderSuccess(cmd + "<br>" + m.get("confStr.runFail") + "<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return renderSuccess(m.get("confStr.runFail") + "<br>" + e.getMessage().replace("\n", "<br>"));
		}
	}

	private String buildRealCmd(String cmd) {
		String dir = "";
		if (StrUtil.isNotEmpty(settingService.get("nginxDir"))) {
			dir = " -p " + settingService.get("nginxDir");
		}

		switch (cmd) {
		case "net start nginx":
		case "service nginx start":
		case "systemctl start nginx":
		case "net stop nginx":
		case "service nginx stop":
		case "systemctl stop nginx":
		case "taskkill /f /im nginx.exe":
		case "pkill nginx":
			return cmd;

		case "stopNormal":
			return settingService.get("nginxExe") + " -s stop" + dir;
		case "startNormal":
			return settingService.get("nginxExe") + " -c " + settingService.get("nginxPath") + dir;
		}

		return null;
	}

//	// 仅执行nginx相关的命令，而不是其他的恶意命令
//	private boolean isAvailableCmd(String cmd) {
//		// 过滤数据库中的路径
//		String nginxPath = ToolUtils.handleConf(settingService.get("nginxPath"));
//		settingService.set("nginxPath", nginxPath);
//		String nginxExe = ToolUtils.handleConf(settingService.get("nginxExe"));
//		settingService.set("nginxExe", nginxExe);
//		String nginxDir = ToolUtils.handleConf(settingService.get("nginxDir"));
//		settingService.set("nginxDir", nginxDir);
//
//		// 检查命令格式
//		switch (cmd) {
//		case "net start nginx":
//		case "service nginx start":
//		case "systemctl start nginx":
//		case "net stop nginx":
//		case "service nginx stop":
//		case "systemctl stop nginx":
//		case "taskkill /f /im nginx.exe":
//		case "pkill nginx":
//			return true;
//		default:
//			break;
//		}
//
//		String dir = "";
//		if (StrUtil.isNotEmpty(settingService.get("nginxDir"))) {
//			dir = " -p " + settingService.get("nginxDir");
//		}
//
//		if (cmd.equals(settingService.get("nginxExe") + " -s stop" + dir)) {
//			return true;
//		}
//
//		if (cmd.equals(settingService.get("nginxExe") + " -c " + settingService.get("nginxPath") + dir)) {
//			return true;
//		}
//
//		return false;
//	}

	@Mapping(value = "getLastCmd")
	public JsonResult getLastCmd(String type) {
		return renderSuccess(settingService.get(type));
	}

	@Mapping(value = "loadConf")
	public JsonResult loadConf() {
		String decompose = settingService.get("decompose");

		ConfExt confExt = confService.buildConf(StrUtil.isNotEmpty(decompose) && decompose.equals("true"), false);
		return renderSuccess(confExt);
	}

	@Mapping(value = "loadOrg")
	public JsonResult loadOrg(String nginxPath) {
		String decompose = settingService.get("decompose");
		ConfExt confExt = confService.buildConf(StrUtil.isNotEmpty(decompose) && decompose.equals("true"), false);

		if (StrUtil.isNotEmpty(nginxPath) && FileUtil.exist(nginxPath) && FileUtil.isFile(nginxPath)) {
			String orgStr = FileUtil.readString(nginxPath, StandardCharsets.UTF_8);
			confExt.setConf(orgStr);

			for (ConfFile confFile : confExt.getFileList()) {
				confFile.setConf("");

				String filePath = new File(nginxPath).getParent() + "/conf.d/" + confFile.getName();
				if (FileUtil.exist(filePath)) {
					confFile.setConf(FileUtil.readString(filePath, StandardCharsets.UTF_8));
				}
			}

			return renderSuccess(confExt);
		} else {
			if (FileUtil.isDirectory(nginxPath)) {
				return renderError(m.get("confStr.error2"));
			}

			return renderError(m.get("confStr.notExist"));
		}

	}

	@Mapping(value = "decompose")
	public JsonResult decompose(String decompose) {
		settingService.set("decompose", decompose);
		return renderSuccess();
	}

	@Mapping(value = "update")
	public JsonResult update() {
		versionConfig.checkVersion();
		if (Integer.parseInt(versionConfig.currentVersion.replace(".", "").replace("v", "")) < Integer.parseInt(versionConfig.newVersion.getVersion().replace(".", "").replace("v", ""))) {
			mainController.autoUpdate(versionConfig.newVersion.getUrl());
			return renderSuccess(m.get("confStr.updateSuccess"));
		} else {
			return renderSuccess(m.get("confStr.noNeedUpdate"));
		}
	}

	@Mapping(value = "getKey")
	public JsonResult getKey(String key) {
		return renderSuccess(settingService.get(key));
	}

	@Mapping(value = "setKey")
	public JsonResult setKey(String key, String val) {
		settingService.set(key, val);
		return renderSuccess();
	}

}
