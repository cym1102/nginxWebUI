package com.cym.controller.adminPage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.InitConfig;
import com.cym.model.Remote;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JarUtil;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;
import com.cym.utils.UpdateUtils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

@Mapping("")
@Controller
public class MainController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	UpdateUtils updateUtils;
	@Inject
	SettingService settingService;

	private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView, String keywords) {
		modelAndView.view("/adminPage/index.html");
		return modelAndView;
	}
	
	@Mapping("doc.html")
	public void doc(Context context) {
		context.redirect("doc/api.html");
	}

	
	@Mapping("/adminPage/main/upload")
	public JsonResult upload(Context context, UploadedFile file) {
		try {
			File temp = new File(FileUtil.getTmpDir() + "/" + file.name);
			file.transferTo(temp);

			// 移动文件
			File dest = new File(homeConfig.home + "cert/" + file.name);
			while(FileUtil.exist(dest)) {
				dest = new File(dest.getPath() + "_1");
			}
			FileUtil.move(temp, dest, true);

			String localType = (String) context.session("localType");
			if ("remote".equals(localType)) {
				Remote remote = (Remote) context.session("remote");

				HashMap<String, Object> paramMap = new HashMap<>();
				paramMap.put("file", dest);

				String rs = HttpUtil.post(remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort() + "/upload", paramMap);
				JsonResult jsonResult = JSONUtil.toBean(rs, JsonResult.class);
				FileUtil.del(dest);
				return jsonResult;
			}

			return renderSuccess(dest.getPath().replace("\\", "/"));
		} catch (IllegalStateException | IOException e) {
			logger.error(e.getMessage(), e);
		}

		return renderError();
	}

	@Mapping("/adminPage/main/autoUpdate")
	public JsonResult autoUpdate(String url) {
//		if (!SystemTool.isLinux()) {
//			return renderError(m.get("commonStr.updateTips"));
//		}

		File jar = JarUtil.getCurrentFile();
		String path = jar.getParent() + "/nginxWebUI.jar.update";
		LOG.info("download:" + path);
		HttpUtil.downloadFile(url, path);
		updateUtils.run(path);
		return renderSuccess();
	}

	@Mapping("/adminPage/main/changeLang")
	public JsonResult changeLang() {
		if (settingService.get("lang") != null && settingService.get("lang").equals("en_US")) {
			settingService.set("lang", "");
		} else {
			settingService.set("lang", "en_US");
		}

		return renderSuccess();
	}

}