package com.cym.controller.adminPage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.model.Ssh;
import com.cym.service.SshService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.craccd.sqlHelper.bean.Page;

@RequestMapping("/adminPage/ssh")
@Controller
public class SshController extends BaseController {
	@Autowired
	SshService sshService;

	@RequestMapping("")
	public ModelAndView index(ModelAndView modelAndView, Page page) {
		page = sshService.search(page);

		modelAndView.addObject("page", page);
		modelAndView.setViewName("/adminPage/ssh/index");
		return modelAndView;
	}

	@RequestMapping("addOver")
	@ResponseBody
	public JsonResult addOver(Ssh ssh) {

		sqlHelper.insertOrUpdate(ssh);

		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, Ssh.class));
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		sqlHelper.deleteById(id, Ssh.class);

		return renderSuccess();
	}

	@RequestMapping("webssh")
	public ModelAndView webssh(ModelAndView modelAndView,String id) {
		Ssh ssh = sqlHelper.findById(id, Ssh.class);
		
		modelAndView.addObject("ssh",ssh);
		modelAndView.setViewName("/adminPage/ssh/webssh");
		return modelAndView;
	}
}
