package com.cym.controller.adminPage;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.InitConfig;
import com.cym.config.VersionConfig;
import com.cym.ext.ConfExt;
import com.cym.ext.ConfFile;
import com.cym.service.ConfService;
import com.cym.service.ServerService;
import com.cym.service.SettingService;
import com.cym.service.UpstreamService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.NginxUtils;
import com.cym.utils.SystemTool;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

@Controller
@RequestMapping("/adminPage/conf")
public class ConfController extends BaseController {
	@Autowired
	UpstreamService upstreamService;
	@Autowired
	SettingService settingService;
	@Autowired
	ServerService serverService;
	@Autowired
	ConfService confService;
	@Autowired
	MainController mainController;

	@Autowired
	VersionConfig versionConfig;
	@Value("${project.version}")
	String currentVersion;

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

		modelAndView.addObject("tmp", InitConfig.home + "temp/nginx.conf");

		modelAndView.setViewName("/adminPage/conf/index");
		return modelAndView;
	}

	@RequestMapping(value = "nginxStatus")
	@ResponseBody
	public JsonResult nginxStatus() {
		if (NginxUtils.isRun()) {
			return renderSuccess(m.get("confStr.nginxStatus") + "：<span class='green'>" + m.get("confStr.running") + "</span>");
		} else {
			return renderSuccess(m.get("confStr.nginxStatus") + "：<span class='red'>" + m.get("confStr.stopped") + "</span>");
		}

	}

	@RequestMapping(value = "replace")
	@ResponseBody
	public JsonResult replace(String json) {

		if (StrUtil.isEmpty(json)) {
			json = getReplaceJson();
		}

		JSONObject jsonObject = JSONUtil.parseObj(json);

		String nginxPath = jsonObject.getStr("nginxPath");
		String nginxContent = Base64.decodeStr(jsonObject.getStr("nginxContent"), CharsetUtil.CHARSET_UTF_8);
		nginxContent = URLDecoder.decode(nginxContent, CharsetUtil.CHARSET_UTF_8).replace("<wave>", "~");

		List<String> subContent = jsonObject.getJSONArray("subContent").toList(String.class);
		for (int i = 0; i < subContent.size(); i++) {
			String content = Base64.decodeStr(subContent.get(i), CharsetUtil.CHARSET_UTF_8);
			content = URLDecoder.decode(content, CharsetUtil.CHARSET_UTF_8).replace("<wave>", "~");
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
		
//		if (!FileUtil.exist(nginxPath)) {
//			return renderError(m.get("confStr.error1"));
//		}
		
		try {
			confService.replace(nginxPath, nginxContent, subContent, subName);
			return renderSuccess(m.get("confStr.replaceSuccess"));
		} catch (Exception e) {
			e.printStackTrace();

			return renderError(m.get("confStr.error3") + ":" + e.getMessage());
		}

	}

	public String getReplaceJson() {
		String decompose = settingService.get("decompose");
		ConfExt confExt = confService.buildConf(StrUtil.isNotEmpty(decompose) && decompose.equals("true"), false);

		URLEncoder urlEncoder = new URLEncoder();

		JSONObject jsonObject = new JSONObject();
		jsonObject.set("nginxContent", Base64.encode(urlEncoder.encode(confExt.getConf(), CharsetUtil.CHARSET_UTF_8)));
		jsonObject.set("subContent", new JSONArray());
		jsonObject.set("subName", new JSONArray());
		for (ConfFile confFile : confExt.getFileList()) {
			jsonObject.getJSONArray("subContent").add(Base64.encode(urlEncoder.encode(confFile.getConf(), CharsetUtil.CHARSET_UTF_8)));
			jsonObject.getJSONArray("subName").add(confFile.getName());
		}
		return jsonObject.toStringPretty();
	}

	@RequestMapping(value = "check")
	@ResponseBody
	public JsonResult check(String nginxPath, String nginxExe, String nginxDir) {
		if (nginxExe == null) {
			nginxExe = settingService.get("nginxExe");
		}
		if (nginxDir == null) {
			nginxDir = settingService.get("nginxDir");
		}

		String decompose = settingService.get("decompose");

		String rs = null;
		String cmd = null;

		FileUtil.del(InitConfig.home + "temp");
		String fileTemp = InitConfig.home + "temp/nginx.conf";

		try {
			ConfExt confExt = confService.buildConf(StrUtil.isNotEmpty(decompose) && decompose.equals("true"), true);
			FileUtil.writeString(confExt.getConf(), fileTemp, CharsetUtil.CHARSET_UTF_8);

			ClassPathResource resource = new ClassPathResource("mime.types");
			FileUtil.writeFromStream(resource.getInputStream(), InitConfig.home + "temp/mime.types");

			for (int i = 0; i < confExt.getFileList().size(); i++) {
				String subName = confExt.getFileList().get(i).getName();
				String subContent = confExt.getFileList().get(i).getConf();

				String tagert = fileTemp.replace("nginx.conf", "conf.d/" + subName).replace(" ", "_");
				FileUtil.writeString(subContent, tagert, StandardCharsets.UTF_8); // 清空
			}

			if (SystemTool.isWindows()) {
				cmd = nginxExe + " -t -c " + fileTemp + " -p " + nginxDir;
			} else {
				cmd = nginxExe + " -t -c " + fileTemp;
				if (StrUtil.isNotEmpty(nginxDir)) {
					cmd += " -p " + nginxDir;
				}
			}
			rs = RuntimeUtil.execForStr(cmd);
		} catch (Exception e) {
			e.printStackTrace();
			rs = e.getMessage().replace("\n", "<br>");
		}

		cmd = "<span class='blue'>" + cmd + "</span>";
		if (rs.contains("successful")) {
			return renderSuccess(cmd + "<br>" + m.get("confStr.verifySuccess") + "<br>" + rs.replace("\n", "<br>"));
		} else {
			return renderError(cmd + "<br>" + m.get("confStr.verifyFail") + "<br>" + rs.replace("\n", "<br>"));
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
			String cmd = nginxExe + " -s reload -c " + nginxPath;
			if (StrUtil.isNotEmpty(nginxDir)) {
				cmd += " -p " + nginxDir;
			}
			String rs = RuntimeUtil.execForStr(cmd);

			cmd = "<span class='blue'>" + cmd + "</span>";
			if (StrUtil.isEmpty(rs) || rs.contains("signal process started")) {
				return renderSuccess(cmd + "<br>" + m.get("confStr.reloadSuccess") + "<br>" + rs.replace("\n", "<br>"));
			} else {
				if (rs.contains("The system cannot find the file specified") || rs.contains("nginx.pid") || rs.contains("PID")) {
					rs = rs + m.get("confStr.mayNotRun");
				}

				return renderError(cmd + "<br>" + m.get("confStr.reloadFail") + "<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError(m.get("confStr.reloadFail") + "<br>" + e.getMessage().replace("\n", "<br>"));
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
				cmd = nginxExe + " -c " + nginxPath;
				if (StrUtil.isNotEmpty(nginxDir)) {
					cmd += " -p " + nginxDir;
				}
				rs = RuntimeUtil.execForStr(cmd);
			}

			cmd = "<span class='blue'>" + cmd + "</span>";
			if (StrUtil.isEmpty(rs) || rs.contains("signal process started")) {
				return renderSuccess(cmd + "<br>" + m.get("confStr.startSuccess") + "<br>" + rs.replace("\n", "<br>"));
			} else {
				return renderError(cmd + "<br>" + m.get("confStr.startFail") + "<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError(m.get("confStr.startFail") + "<br>" + e.getMessage().replace("\n", "<br>"));
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
			String cmd;
			if (SystemTool.isWindows()) {
				cmd = "taskkill /im /f nginx.exe ";
			} else {
				cmd = "pkill nginx";
			}
			String rs = RuntimeUtil.execForStr(cmd);

			cmd = "<span class='blue'>" + cmd + "</span>";
			if (StrUtil.isEmpty(rs) || rs.contains("已终止进程") || rs.toLowerCase().contains("terminated process")) {
				return renderSuccess(cmd + "<br>" + m.get("confStr.stopSuccess") + "<br>" + rs.replace("\n", "<br>"));
			} else {
				return renderError(cmd + "<br>" + m.get("confStr.stopFail") + "<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError(m.get("confStr.stopFail") + "<br>" + e.getMessage().replace("\n", "<br>"));
		}
	}

	@RequestMapping(value = "runCmd")
	@ResponseBody
	public JsonResult runCmd(String cmd, String type) {
		settingService.set(type, cmd);

		try {
			String rs = "";
			if (cmd.contains(".exe")) {
				RuntimeUtil.exec(cmd);
			} else {
				rs = RuntimeUtil.execForStr(cmd);
			}

			cmd = "<span class='blue'>" + cmd + "</span>";
			if (StrUtil.isEmpty(rs) || rs.contains("已终止进程") || rs.contains("signal process started") || rs.toLowerCase().contains("terminated process")) {
				return renderSuccess(cmd + "<br>" + m.get("confStr.runSuccess") + "<br>" + rs.replace("\n", "<br>"));
			} else {
				return renderError(cmd + "<br>" + m.get("confStr.runFail") + "<br>" + rs.replace("\n", "<br>"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return renderError(m.get("confStr.runFail") + "<br>" + e.getMessage().replace("\n", "<br>"));
		}
	}

	@RequestMapping(value = "getLastCmd")
	@ResponseBody
	public JsonResult getLastCmd(String type) {
		return renderSuccess(settingService.get(type));
	}

	@RequestMapping(value = "loadConf")
	@ResponseBody
	public JsonResult loadConf() {
		String decompose = settingService.get("decompose");

		ConfExt confExt = confService.buildConf(StrUtil.isNotEmpty(decompose) && decompose.equals("true"), false);
		return renderSuccess(confExt);
	}

	@RequestMapping(value = "loadOrg")
	@ResponseBody
	public JsonResult loadOrg(String nginxPath) {
		String decompose = settingService.get("decompose");
		ConfExt confExt = confService.buildConf(StrUtil.isNotEmpty(decompose) && decompose.equals("true"), false);

		if (StrUtil.isNotEmpty(nginxPath) && FileUtil.exist(nginxPath) && FileUtil.isFile(nginxPath)) {
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
			if (FileUtil.isDirectory(nginxPath)) {
				return renderError(m.get("confStr.error2"));
			}

			return renderError(m.get("confStr.notExist"));
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
			return renderSuccess(m.get("confStr.updateSuccess"));
		} else {
			return renderSuccess(m.get("confStr.noNeedUpdate"));
		}
	}

	@RequestMapping(value = "getKey")
	@ResponseBody
	public JsonResult getKey(String key) {
		return renderSuccess(settingService.get(key));
	}

	@RequestMapping(value = "setKey")
	@ResponseBody
	public JsonResult setKey(String key, String val) {
		settingService.set(key, val);
		return renderSuccess();
	}

}
