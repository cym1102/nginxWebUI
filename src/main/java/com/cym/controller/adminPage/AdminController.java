package com.cym.controller.adminPage;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.Admin;
import com.cym.service.AdminService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SendMailUtils;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.core.util.StrUtil;

@Controller
@RequestMapping("/adminPage/admin")
public class AdminController extends BaseController {
	@Autowired
	AdminService adminService;
	@Autowired
	SettingService settingService;
	@Autowired
	SendMailUtils sendCloudUtils;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView, Page page) {
		page = adminService.search(page);

		modelAndView.addObject("page", page);
		modelAndView.setViewName("/adminPage/admin/index");
		return modelAndView;
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Admin admin) {
		if (StrUtil.isEmpty(admin.getId())) {
			Long count = adminService.getCountByName(admin.getName());
			if (count > 0) {
				return renderError("与已有用户重名");
			}
		}else {
			Long count = adminService.getCountByNameWithOutId(admin.getName(), admin.getId());
			if (count > 0) {
				return renderError("与已有用户重名");
			}
		}
		
		sqlHelper.insertOrUpdate(admin);

		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, Admin.class));
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		sqlHelper.deleteById(id, Admin.class);

		return renderSuccess();
	}

	@RequestMapping("getMailSetting")
	@ResponseBody
	public JsonResult getMailSetting() {
		Map<String, String> map = new HashMap<>();


		map.put("mail_host", settingService.get("mail_host"));
		map.put("mail_port", settingService.get("mail_port"));
		map.put("mail_from", settingService.get("mail_from"));
		map.put("mail_user", settingService.get("mail_user"));
		map.put("mail_pass", settingService.get("mail_pass"));
		map.put("mail_ssl", settingService.get("mail_ssl"));

		return renderSuccess(map);
	}

	@RequestMapping("updateMailSetting")
	@ResponseBody
	public JsonResult updateMailSetting(String mailType, String mail_user, String mail_host, String mail_port, String mail_from, String mail_pass, String mail_ssl) {
		settingService.set("mail_host", mail_host);
		settingService.set("mail_port", mail_port);
		settingService.set("mail_user", mail_user);
		settingService.set("mail_from", mail_from);
		settingService.set("mail_pass", mail_pass);
		settingService.set("mail_ssl", mail_ssl);

		return renderSuccess();
	}

	@RequestMapping("testMail")
	@ResponseBody
	public JsonResult testMail(String mail) {
		if(StrUtil.isEmpty(mail)) {
			return renderError("邮箱为空");
		}
		try {
			sendCloudUtils.sendMailSmtp(mail, "nginxWebUI测试邮件", "nginxWebUI测试邮件");
			return renderSuccess();
		} catch (Exception e) {
			e.printStackTrace();
			return renderError(e.getMessage());
		}
	}
}
