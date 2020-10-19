package com.cym.controller.adminPage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.Admin;
import com.cym.service.AdminService;
import com.cym.service.SettingService;
import com.cym.utils.AuthUtils;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SendMailUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

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
	@Autowired
	AuthUtils authUtils;

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
				return renderError(m.get("adminStr.nameRepetition"));
			}
		} else {
			Long count = adminService.getCountByNameWithOutId(admin.getName(), admin.getId());
			if (count > 0) {
				return renderError(m.get("adminStr.nameRepetition"));
			}
		}

		if (admin.getAuth()) {
			admin.setKey(authUtils.makeKey());
		} else {
			admin.setKey("");
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
		map.put("mail_interval", settingService.get("mail_interval"));
		
		return renderSuccess(map);
	}

	@RequestMapping("updateMailSetting")
	@ResponseBody
	public JsonResult updateMailSetting(String mailType, String mail_user, String mail_host, String mail_port, String mail_from, String mail_pass, String mail_ssl,String mail_interval) {
		settingService.set("mail_host", mail_host);
		settingService.set("mail_port", mail_port);
		settingService.set("mail_user", mail_user);
		settingService.set("mail_from", mail_from);
		settingService.set("mail_pass", mail_pass);
		settingService.set("mail_ssl", mail_ssl);
		settingService.set("mail_interval", mail_interval);
		
		return renderSuccess();
	}

	@RequestMapping("testMail")
	@ResponseBody
	public JsonResult testMail(String mail) {
		if (StrUtil.isEmpty(mail)) {
			return renderError(m.get("adminStr.emailEmpty"));
		}
		try {
			sendCloudUtils.sendMailSmtp(mail, m.get("adminStr.emailTest"), m.get("adminStr.emailTest"));
			return renderSuccess();
		} catch (Exception e) {
			e.printStackTrace();
			return renderError(e.getMessage());
		}
	}
	
	
	@RequestMapping("testAuth")
	@ResponseBody
	public JsonResult testAuth(String key, String code) {
		
		Boolean rs = authUtils.testKey(key, code);
		return renderSuccess(rs);
	}

	@RequestMapping(value = "qr")
	public void getqcode(HttpServletResponse resp, String url, Integer w, Integer h) throws IOException {
		if (url != null && !"".equals(url)) {
			ServletOutputStream stream = null;

			if (w == null) {
				w = 300;
			}
			if (h == null) {
				h = 300;
			}
			try {
				stream = resp.getOutputStream();

				Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
				hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
				hints.put(EncodeHintType.MARGIN, 0);

				BitMatrix matrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, w, h, hints);
				MatrixToImageWriter.writeToStream(matrix, "png", stream);
			} catch (WriterException e) {
				e.printStackTrace();
			} finally {
				if (stream != null) {
					stream.flush();
					stream.close();
				}
			}
		}
	}
}
