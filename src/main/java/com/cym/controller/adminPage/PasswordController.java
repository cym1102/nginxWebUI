package com.cym.controller.adminPage;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.InitConfig;
import com.cym.model.Password;
import com.cym.service.PasswordService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

@RequestMapping("/adminPage/password")
@Controller
public class PasswordController extends BaseController {
	@Autowired
	PasswordService passwordService;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView, Page page) {
		page = passwordService.search(page);

		modelAndView.addObject("page", page);
		modelAndView.setViewName("/adminPage/password/index");
		return modelAndView;
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Password password) throws IOException {

		if (StrUtil.isEmpty(password.getId())) {
			Long count = passwordService.getCountByName(password.getName());
			if (count > 0) {
				return renderError(m.get("adminStr.nameRepetition"));
			}
		} else {
			Long count = passwordService.getCountByNameWithOutId(password.getName(), password.getId());
			if (count > 0) {
				return renderError(m.get("adminStr.nameRepetition"));
			}

			Password passwordOrg = sqlHelper.findById(password.getId(), Password.class);
			FileUtil.del(passwordOrg.getPath());
		}

		password.setPath(InitConfig.home + "password/" + password.getName());
//		CryptUtils.make(password.getName(), password.getPass(), password.getPath());
		FileUtil.writeString( password.getName() + ":" +  password.getPass(), password.getPath(), "UTF-8");

		sqlHelper.insertOrUpdate(password);

		return renderSuccess();
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		Password password = sqlHelper.findById(id, Password.class);
		sqlHelper.deleteById(id, Password.class);
		FileUtil.del(password.getPath());
		
		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, Password.class));
	}
}
