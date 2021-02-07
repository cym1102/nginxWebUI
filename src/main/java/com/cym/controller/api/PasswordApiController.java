package com.cym.controller.api;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cym.controller.adminPage.PasswordController;
import com.cym.model.Password;
import com.cym.service.PasswordService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;

import cn.hutool.core.io.FileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "密码文件接口")
@RestController
@RequestMapping("/api/password")
public class PasswordApiController extends BaseController{
	@Autowired
	PasswordService passwordService;
	@Autowired
	PasswordController passwordController;
	
	@ApiOperation("获取全部密码文件列表")
	@PostMapping("getList")
	public JsonResult<List<Password>> getList() {
		List<Password> list = sqlHelper.findAll(Password.class);
		return renderSuccess(list);
	}

	@ApiOperation("添加或编辑密码文件")
	@PostMapping("insertOrUpdate")
	public JsonResult<?> insertOrUpdate(Password password) throws IOException {
		return renderSuccess(passwordController.addOver(password));
	}

	@ApiOperation("删除密码文件")
	@PostMapping("del")
	public JsonResult<?> del(String id) {
		Password password = sqlHelper.findById(id, Password.class);
		sqlHelper.deleteById(id, Password.class);
		FileUtil.del(password.getPath());

		return renderSuccess();
	}

}
