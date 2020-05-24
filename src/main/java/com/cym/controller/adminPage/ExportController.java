package com.cym.controller.adminPage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import com.cym.ext.AsycPack;
import com.cym.service.ConfService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.date.DateUtil;
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

		AsycPack asycPack = confService.getAsycPack();
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
	public JsonResult dataImport(String json) {
		AsycPack asycPack = JSONUtil.toBean(json, AsycPack.class);

		confService.setAsycPack(asycPack);

		return renderSuccess();
	}

}
