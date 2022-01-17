package com.cym.controller.adminPage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.DownloadedFile;
import org.noear.solon.core.handle.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.config.InitConfig;
import com.cym.ext.AsycPack;
import com.cym.model.Admin;
import com.cym.service.ConfService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Controller
@Mapping("/adminPage/export")
public class ExportController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	ConfService confService;

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView) {

		modelAndView.view("/adminPage/export/index.html");
		return modelAndView;
	}

	@Mapping("dataExport")
	public DownloadedFile dataExport(Context context) throws IOException {
		String date = DateUtil.format(new Date(), "yyyy-MM-dd_HH-mm-ss");

		AsycPack asycPack = confService.getAsycPack(new String[] { "all" });
		String json = JSONUtil.toJsonPrettyStr(asycPack);

//		context.header("Content-Type", "application/octet-stream");
//		context.header("content-disposition", "attachment;filename=" + URLEncoder.encode(date + ".json", "UTF-8")); // 设置文件名
//
//		byte[] buffer = new byte[1024];
//		BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));
//		OutputStream os = context.outputStream();
//		int i = bis.read(buffer);
//		while (i != -1) {
//			os.write(buffer, 0, i);
//			i = bis.read(buffer);
//		}

		DownloadedFile downloadedFile = new DownloadedFile("application/octet-stream", new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))), date + ".json");
		return downloadedFile;
	}

	@Mapping(value = "dataImport")
	public JsonResult dataImport(String json, Context context, String adminName) {
		AsycPack asycPack = JSONUtil.toBean(json, AsycPack.class);
		if (StrUtil.isEmpty(adminName)) {
			Admin admin = getAdmin();
			adminName = admin.getName();
		}
		confService.setAsycPack(asycPack, adminName);

		return renderSuccess();
	}

	@Mapping("logExport")
	public DownloadedFile logExport(Context context) throws FileNotFoundException {
		File file = new File(homeConfig.home + "log/nginxWebUI.log");
		if (file.exists()) {
			DownloadedFile downloadedFile = new DownloadedFile("application/octet-stream", new FileInputStream(file), file.getName());
			return downloadedFile;
		}
		
		return null;
	}

}
