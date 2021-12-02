package com.cym.controller.adminPage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.Bak;
import com.cym.model.BakSub;
import com.cym.service.BakService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

@Controller
@RequestMapping("/adminPage/bak")
public class BakController extends BaseController {
	@Autowired
	SettingService settingService;
	@Autowired
	BakService bakService;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView, Page page) {
		page = bakService.getList(page);

		modelAndView.addObject("page", page);
		modelAndView.setViewName("/adminPage/bak/index");
		return modelAndView;
	}

	@RequestMapping("getCompare")
	@ResponseBody
	public JsonResult getCompare(String id) {
		Bak bak = sqlHelper.findById(id, Bak.class);

		Bak pre = bakService.getPre(id);
		if (pre == null) {
			return renderError("没有更早的备份文件");
		}

		Map map = new HashMap<>();
		map.put("bak", bak);
		map.put("pre", pre);

		return renderSuccess(map);
	}

	@RequestMapping("content")
	@ResponseBody
	public JsonResult content(String id) {
		Bak bak = sqlHelper.findById(id, Bak.class);
		return renderSuccess(bak);
	}

	@RequestMapping("replace")
	@ResponseBody
	public JsonResult replace(String id) {
		Bak bak = sqlHelper.findById(id, Bak.class);

		String nginxPath = settingService.get("nginxPath");

		if (StrUtil.isNotEmpty(nginxPath)) {
			File pathFile = new File(nginxPath);
			// 写入主文件
			FileUtil.writeString(bak.getContent(), pathFile, StandardCharsets.UTF_8);

			// 写入子文件
			String confd = pathFile.getParent() + File.separator + "conf.d" + File.separator;
			FileUtil.del(confd);
			FileUtil.mkdir(confd);

			List<BakSub> subList = bakService.getSubList(bak.getId());
			for (BakSub bakSub : subList) {
				FileUtil.writeString(bakSub.getContent(), confd + bakSub.getName(), StandardCharsets.UTF_8);
			}
			return renderSuccess();
		} else {
			return renderError(m.get("bakStr.pathNotice"));
		}

	}

	@Transactional
	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		bakService.del(id);
		return renderSuccess();
	}

	@RequestMapping("delAll")
	@ResponseBody
	public JsonResult delAll() {
		bakService.delAll();

		return renderSuccess();
	}

}
