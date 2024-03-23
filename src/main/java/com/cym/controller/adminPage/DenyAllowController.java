package com.cym.controller.adminPage;

import java.util.ArrayList;
import java.util.List;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.ext.DenyAllowExt;
import com.cym.ext.ServerExt;
import com.cym.model.Admin;
import com.cym.model.DenyAllow;
import com.cym.model.Log;
import com.cym.model.Server;
import com.cym.model.Upstream;
import com.cym.service.DenyAllowService;
import com.cym.sqlhelper.bean.Page;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.util.StrUtil;

@Controller
@Mapping("/adminPage/denyAllow")
public class DenyAllowController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	DenyAllowService denyAllowService;

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView, Page page) {

		page = denyAllowService.search(page);

		List<DenyAllowExt> exts = new ArrayList<DenyAllowExt>();
		for (DenyAllow denyAllow : (List<DenyAllow>) page.getRecords()) {
			DenyAllowExt denyAllowExt = new DenyAllowExt();
			denyAllowExt.setDenyAllow(denyAllow);

			if (StrUtil.isBlankIfStr(denyAllow.getIp())) {
				denyAllowExt.setIpCount(0);
			} else {
				denyAllowExt.setIpCount(denyAllow.getIp().split("\n").length);
			}
			exts.add(denyAllowExt);
		}
		page.setRecords(exts);

		modelAndView.put("page", page);
		modelAndView.view("/adminPage/denyAllow/index.html");
		return modelAndView;
	}

	@Mapping("addOver")
	public JsonResult addOver(DenyAllow denyAllow) {
		
		//ip去重
		denyAllowService.removeSame(denyAllow);
		
		sqlHelper.insertOrUpdate(denyAllow);

		return renderSuccess();
	}

	@Mapping("detail")
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, DenyAllow.class));
	}

	@Mapping("del")
	public JsonResult del(String id) {
		String[] ids = id.split(",");
		sqlHelper.deleteByIds(ids, DenyAllow.class);

		return renderSuccess();
	}

}
