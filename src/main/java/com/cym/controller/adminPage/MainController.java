package com.cym.controller.adminPage;

import java.io.File;
import java.io.IOException;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.core.handle.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JarUtil;
import com.cym.utils.JsonResult;
import com.cym.utils.UpdateUtils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;

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
			File temp = new File(FileUtil.getTmpDir() + File.separator + file.getName().replace("..", "")); 
			file.transferTo(temp);

			return renderSuccess(temp.getPath().replace("\\", "/"));
		} catch (IllegalStateException | IOException e) {
			logger.error(e.getMessage(), e);
		}

		return renderError();
	}

	@Mapping("/adminPage/main/autoUpdate")
	public JsonResult autoUpdate(String url) {
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