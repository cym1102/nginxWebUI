package com.cym.controller.adminPage;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.ScheduleTask;
import com.cym.model.Bak;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;

@Controller
@RequestMapping("/adminPage/bak")
public class BakController extends BaseController {
	@Autowired
	SettingService settingService;
	
	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) {
		List<Bak> bakList = getBakList();

		CollectionUtil.sort(bakList, new Comparator<Bak>() {

			@Override
			public int compare(Bak o1, Bak o2) {
				return StrUtil.compare(o2.getTime(), o1.getTime(), true);
			}
		});
		
		modelAndView.addObject("bakList", bakList);
		modelAndView.setViewName("/adminPage/bak/index");
		return modelAndView;
	}

	private List<Bak> getBakList() {
		List<Bak> list = new ArrayList<Bak>();

		String nginxPath = settingService.get("nginxPath");
		if (StrUtil.isNotEmpty(nginxPath) && FileUtil.exist(nginxPath)) {
			File dir = new File(nginxPath).getParentFile();

			File[] fileList = dir.listFiles();
			for (File file : fileList) {
				if (file.getName().contains("nginx.conf") && file.getName().endsWith(".bak")) {
					Bak bak = new Bak();
					bak.setPath(file.getPath().replace("\\", "/"));
					DateTime date = DateUtil.parse(file.getName().replace("nginx.conf", "").replace(".bak", ""), "yyyy-MM-dd_HH-mm-ss");
					bak.setTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));

					list.add(bak);
				}
			}
		}

		return list;
	}

	@RequestMapping("content")
	@ResponseBody
	public JsonResult content(String path) {
		String str = FileUtil.readString(path, Charset.forName("UTF-8"));
		return renderSuccess(str);
	}

	@RequestMapping("replace")
	@ResponseBody
	public JsonResult replace(String path) {
		String nginxPath = settingService.get("nginxPath");

		String str = FileUtil.readString(path, Charset.forName("UTF-8"));

		if (StrUtil.isNotEmpty(nginxPath)) {
			FileUtil.writeString(str, nginxPath, Charset.forName("UTF-8"));

			if (FileUtil.exist(path.replace(".bak", ".zip"))) {
				String confd = nginxPath.replace("nginx.conf", "conf.d/");
				FileUtil.del(confd);
				ZipUtil.unzip(path.replace(".bak", ".zip"));
				if (FileUtil.exist(path.replace(".bak", ""))) {
					FileUtil.rename(new File(path.replace(".bak", "")), confd, false, true);
				}
			}

			return renderSuccess();
		} else {
			return renderError("conf文件路径未配置");
		}

	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String path) {
		FileUtil.del(path);
		FileUtil.del(path.replace(".bak", ".zip"));
		return renderSuccess();
	}
	
	@RequestMapping("delAll")
	@ResponseBody
	public JsonResult delAll() {
		List<Bak> list = getBakList();
		for(Bak bak:list) {
			del(bak.getPath());
		}
		
		return renderSuccess();
	}

}
