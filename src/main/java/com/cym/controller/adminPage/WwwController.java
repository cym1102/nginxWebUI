package com.cym.controller.adminPage;

import java.io.File;
import java.nio.charset.Charset;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.InitConfig;
import com.cym.model.Www;
import com.cym.service.WwwService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;

@RequestMapping("/adminPage/www")
@Controller
public class WwwController extends BaseController {
	@Autowired
	WwwService wwwService;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView) {

		modelAndView.addObject("list", sqlHelper.findAll(new Sort("dir", Direction.ASC), Www.class));
		modelAndView.setViewName("/adminPage/www/index");
		return modelAndView;
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Www www) {
		if (wwwService.hasName(www.getName())) {
			return renderError("名称重复");
		}

		try {
			if (StrUtil.isNotEmpty(www.getDir())) {
				String dir = InitConfig.home + "wwww/" + www.getName();
				try {
					ZipUtil.unzip(www.getDir(), dir);
				} catch (Exception e) {
					// 默认UTF-8下不能解压中文字符, 尝试使用gbk
					ZipUtil.unzip(www.getDir(), dir, Charset.forName("GBK"));
				}

				FileUtil.del(www.getDir());
				www.setDir(dir);
			} else {
				// 修改名称, 也要修改文件夹名
				Www wwwOrg = sqlHelper.findById(www.getId(), Www.class);
				FileUtil.rename(new File(wwwOrg.getDir()),  InitConfig.home + "wwww/" + www.getName(), true);
				
				www.setDir( InitConfig.home + "wwww/" + www.getName());
			}
			sqlHelper.insertOrUpdate(www);
			
			return renderSuccess();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return renderError("解压错误，请确认压缩包为zip格式");
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		Www www = sqlHelper.findById(id, Www.class);
		sqlHelper.deleteById(id, Www.class);
		if (StrUtil.isNotEmpty(www.getDir()) && www.getDir() != "/") {
			FileUtil.del(www.getDir());
		}

		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		Www www = sqlHelper.findById(id, Www.class);

		return renderSuccess(www);
	}
}
