package com.cym.controller.adminPage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.DenyAllow;
import com.cym.service.DenyAllowService;
import com.cym.service.SshService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Page;

@RequestMapping("/adminPage/denyAllow")
@Controller
public class DenyAllowController extends BaseController {
	@Autowired
	DenyAllowService denyAllowService;

	@RequestMapping("")
	public ModelAndView index(ModelAndView modelAndView, Page page) {
		page = denyAllowService.search(page);

		modelAndView.addObject("page", page);
		modelAndView.setViewName("/adminPage/denyAllow/index");
		return modelAndView;
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(DenyAllow denyAllow) {

		sqlHelper.insertOrUpdate(denyAllow);

		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, DenyAllow.class));
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		sqlHelper.deleteById(id, DenyAllow.class);

		return renderSuccess();
	}

}
