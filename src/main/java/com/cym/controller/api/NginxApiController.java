package com.cym.controller.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cym.controller.adminPage.ConfController;
import com.cym.model.Admin;
import com.cym.service.AdminService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.NginxUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "nginx接口")
@RestController
@RequestMapping("/api/nginx")
public class NginxApiController extends BaseController {
	@Autowired
	ConfController confController;
	@Autowired
	AdminService adminService;
	
	@ApiOperation("获取nginx状态")
	@PostMapping("nginxStatus")
	public JsonResult<?> nginxStatus() {
		if (NginxUtils.isRun()) {
			return renderSuccess(m.get("confStr.running"));
		} else {
			return renderError(m.get("confStr.stopped"));
		}
	}

	@ApiOperation("替换conf文件")
	@PostMapping("replace")
	public JsonResult<?> replace(@RequestHeader String token, HttpServletRequest request) {
		JsonResult jsonResult =  confController.replace(confController.getReplaceJson(), request, null);
		if (jsonResult.isSuccess()) {
			return renderSuccess("替换成功");
		} else {
			return renderError("替换失败");
		}
	}

	@ApiOperation("效验conf文件")
	@PostMapping("check")
	public JsonResult<?> checkBase() {
		JsonResult jsonResult = confController.checkBase();
		if (jsonResult.isSuccess()) {
			return renderSuccess("效验成功");
		} else {
			return renderError("效验失败");
		}
	}

	@ApiOperation("重载conf文件")
	@PostMapping("reload")
	public synchronized JsonResult<?> reload() {
		JsonResult jsonResult = confController.reload(null, null, null);
		if (jsonResult.isSuccess()) {
			return renderSuccess("重载成功");
		} else {
			return renderError("重载失败");
		}
		
	}

	@ApiOperation("停止nginx")
	@PostMapping("stop")
	public synchronized JsonResult<?> stop() {
		JsonResult jsonResult = confController.stop(null, null);
		
		if (jsonResult.isSuccess()) {
			return renderSuccess("停止成功");
		} else {
			return renderError("停止失败");
		}
	}

	@ApiOperation("启动nginx")
	@PostMapping("start")
	public synchronized JsonResult<?> start() {
		JsonResult jsonResult = confController.start(null, null, null);
		
		if (jsonResult.isSuccess()) {
			return renderSuccess("启动成功");
		} else {
			return renderError("启动失败");
		}
	}
}
