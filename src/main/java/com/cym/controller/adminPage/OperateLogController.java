package com.cym.controller.adminPage;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;

import com.cym.model.OperateLog;
import com.cym.service.OperateLogService;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

@Controller
@Mapping("/adminPage/operateLog")
public class OperateLogController extends BaseController{
	@Inject
	OperateLogService operateLogService;
	
	@Mapping("")
	public ModelAndView index( ModelAndView modelAndView, Page page) {
		page = operateLogService.search(page);
		
		modelAndView.put("page", page);
		
		modelAndView.view("/adminPage/operatelog/index.html");
		return modelAndView;
	}
	
	
	@Mapping("detail")
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, OperateLog.class));
	}
	
	@Mapping("delAll")
	public JsonResult delAll() {
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), OperateLog.class);

		return renderSuccess();
	}
}
