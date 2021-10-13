package com.cym.controller.adminPage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
@RequestMapping("/adminPage/export")
public class ExportController extends BaseController {
	@Autowired
	ConfService confService;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) {

		modelAndView.setViewName("/adminPage/export/index");
		return modelAndView;
	}

	@RequestMapping("dataExport")
	public void dataExport(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String date = DateUtil.format(new Date(), "yyyy-MM-dd_HH-mm-ss");

		AsycPack asycPack = confService.getAsycPack(new String[] {"all"});
		String json = JSONUtil.toJsonPrettyStr(asycPack);

		response.addHeader("Content-Type", "application/octet-stream");
		response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(date + ".json", "UTF-8")); // 设置文件名

		byte[] buffer = new byte[1024];
		BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));
		OutputStream os = response.getOutputStream();
		int i = bis.read(buffer);
		while (i != -1) {
			os.write(buffer, 0, i);
			i = bis.read(buffer);
		}
	}

	@RequestMapping(value = "dataImport")
	@ResponseBody
	public JsonResult dataImport(String json, HttpServletRequest request, String adminName) {
		AsycPack asycPack = JSONUtil.toBean(json, AsycPack.class);
		if(StrUtil.isEmpty(adminName)) {
			Admin admin = getAdmin(request);
			adminName = admin.getName();
		}
		confService.setAsycPack(asycPack, adminName);

		return renderSuccess();
	}

	@RequestMapping("logExport")
	public void logExport(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		File file = new File(InitConfig.home + "log/nginxWebUI.log");
		if (file.exists()) {
			// 配置文件下载
			response.setHeader("content-type", "application/octet-stream");
			response.setContentType("application/octet-stream");
			// 下载文件能正常显示中文
			response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
			// 实现文件下载
			byte[] buffer = new byte[1024];
			FileInputStream fis = null;
			BufferedInputStream bis = null;
			try {
				fis = new FileInputStream(file);
				bis = new BufferedInputStream(fis);
				OutputStream os = response.getOutputStream();
				int i = bis.read(buffer);
				while (i != -1) {
					os.write(buffer, 0, i);
					i = bis.read(buffer);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bis != null) {
					try {
						bis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

}
