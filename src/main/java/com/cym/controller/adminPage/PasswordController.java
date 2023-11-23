package com.cym.controller.adminPage;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;

import com.cym.model.Password;
import com.cym.service.PasswordService;
import com.cym.sqlhelper.bean.Page;
import com.cym.utils.BaseController;
import com.cym.utils.Crypt;
import com.cym.utils.JsonResult;
import com.cym.utils.SystemTool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

@Mapping("/adminPage/password")
@Controller
public class PasswordController extends BaseController {
	@Inject
	PasswordService passwordService;

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView, Page page) {
		page = passwordService.search(page);

		modelAndView.put("page", page);
		modelAndView.view("/adminPage/password/index.html");
		return modelAndView;
	}

	@Mapping("addOver")
	public JsonResult addOver(Password password){

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

		password.setPath(homeConfig.home + "password/" + password.getName());

		String value = "";
		if (SystemTool.isWindows()) {
			// windows 明码
			value = password.getName() + ":" + password.getPass();
		} else {
			// linux 生成加密
			value = Crypt.getString(password.getName(), password.getPass());
		}
		FileUtil.writeString(value, password.getPath(), "UTF-8");

		sqlHelper.insertOrUpdate(password);

		return renderSuccess();
	}

	@Mapping("del")
	public JsonResult del(String id) {
		Password password = sqlHelper.findById(id, Password.class);
		sqlHelper.deleteById(id, Password.class);
		FileUtil.del(password.getPath());

		return renderSuccess();
	}

	@Mapping("detail")
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, Password.class));
	}
}
