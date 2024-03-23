package com.cym.controller.adminPage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.AppFilter;
import com.cym.model.Log;
import com.cym.service.LogService;
import com.cym.service.SettingService;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.utils.BLogFileTailer;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.URLUtil;

@Controller
@Mapping("/adminPage/log")
public class LogController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	SettingService settingService;
	@Inject
	LogService logService;
	@Inject
	AppFilter appFilter;
	@Inject
	BLogFileTailer bLogFileTailer;

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView, Page page) {
		page = logService.search(page);
		modelAndView.put("page", page);

		modelAndView.put("isLinux", SystemTool.isLinux());
		modelAndView.view("/adminPage/log/index.html");
		return modelAndView;
	}

	@Mapping("addOver")
	public JsonResult addOver(Log log) {
		if (logService.hasDir(log.getPath(), log.getId())) {
			return renderError(m.get("logStr.sameDir"));
		}

		if (FileUtil.isDirectory(log.getPath())) {
			return renderError(m.get("logStr.notFile"));
		}

		sqlHelper.insertOrUpdate(log);
		return renderSuccess();
	}

	@Mapping("detail")
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, Log.class));
	}

	@Mapping("del")
	public JsonResult del(String id) {
		String[] ids = id.split(",");
		sqlHelper.deleteByIds(ids, Log.class);
		return renderSuccess();
	}

	@Mapping("tail")
	public ModelAndView tail(ModelAndView modelAndView, String id, String protocol) {
		modelAndView.put("id", id);
		modelAndView.view("/adminPage/log/tail.html");
		return modelAndView;
	}

	@Mapping("down")
	public void down(ModelAndView modelAndView, String id) throws IOException {
		Log log = sqlHelper.findById(id, Log.class);
		File file = new File(log.getPath());

		Context.current().contentType("application/octet-stream");
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; filename=" + URLUtil.encode(file.getName());
		Context.current().header(headerKey, headerValue);

		InputStream inputStream = Files.newInputStream(file.toPath());
		Context.current().output(inputStream);
	}

	@Mapping("tailCmd")
	public JsonResult tailCmd(String id, String guid) {
		Log log = sqlHelper.findById(id, Log.class);
		if (!FileUtil.exist(log.getPath())) {
			return renderSuccess("");
		}

		String rs = bLogFileTailer.run(guid, log.getPath());
		return renderSuccess(rs);
	}

}
