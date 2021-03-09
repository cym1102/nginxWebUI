package com.cym.controller.adminPage;

import java.net.URL;
import java.nio.charset.Charset;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.Www;
import com.cym.service.WwwService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.hutool.core.io.FileUtil;
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
	public JsonResult addOver(Www www, String dirTemp) {
		if (wwwService.hasDir(www.getDir(),www.getId())) {
			return renderError(m.get("wwwStr.sameDir"));
		}

		try {
			FileUtil.del(www.getDir());
			try {
				ZipUtil.unzip(dirTemp, www.getDir());
			} catch (Exception e) {
				// 默认UTF-8下不能解压中文字符, 尝试使用gbk
				ZipUtil.unzip(dirTemp, www.getDir(), Charset.forName("GBK"));
			}

			FileUtil.del(dirTemp);
			sqlHelper.insertOrUpdate(www);

			return renderSuccess();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return renderError(m.get("wwwStr.zipError"));
	}



	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		sqlHelper.deleteById(id, Www.class);

		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		Www www = sqlHelper.findById(id, Www.class);

		return renderSuccess(www);
	}

	public String getClassPath() throws Exception {
		try {
			String strClassName = getClass().getName();
			String strPackageName = "";
			if (getClass().getPackage() != null) {
				strPackageName = getClass().getPackage().getName();
			}
			String strClassFileName = "";
			if (!"".equals(strPackageName)) {
				strClassFileName = strClassName.substring(strPackageName.length() + 1, strClassName.length());
			} else {
				strClassFileName = strClassName;
			}
			URL url = null;
			url = getClass().getResource(strClassFileName + ".class");
			String strURL = url.toString();
			strURL = strURL.substring(strURL.indexOf('/') + 1, strURL.lastIndexOf('/'));
			// 返回当前类的路径，并且处理路径中的空格，因为在路径中出现的空格如果不处理的话，
			// 在访问时就会从空格处断开，那么也就取不到完整的信息了，这个问题在web开发中尤其要注意
			return strURL.replaceAll("%20", " ");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
}
