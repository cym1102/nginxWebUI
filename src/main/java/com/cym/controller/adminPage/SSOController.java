package com.cym.controller.adminPage;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.cym.ext.AdminExt;
import com.cym.ext.Tree;
import com.cym.model.Admin;
import com.cym.model.Group;
import com.cym.service.AdminService;
import com.cym.service.GroupService;
import com.cym.service.SettingService;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.utils.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.jayway.jsonpath.JsonPath;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@Controller
@Mapping("/adminPage/sso")
public class SSOController extends BaseController {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Inject
	AdminService adminService;
	@Inject
	SettingService settingService;
	@Inject
	SendMailUtils sendCloudUtils;
	@Inject
	AuthUtils authUtils;
	@Inject
	GroupService groupService;
	@Inject
	RemoteController remoteController;

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView) {

		modelAndView.put("codeUrl", settingService.get("sso_codeUrl"));
		modelAndView.put("tokenUrl", settingService.get("sso_tokenUrl"));
		modelAndView.put("userinfoUrl", settingService.get("sso_userinfoUrl"));
		modelAndView.put("jsonpath", settingService.get("sso_jsonpath"));
		modelAndView.put("clientID", settingService.get("sso_clientID"));
		modelAndView.put("clientSecret", settingService.get("sso_clientSecret"));
		modelAndView.view("/adminPage/sso/index.html");
		return modelAndView;
	}

	@Mapping("save")
	public JsonResult save(String codeUrl, String tokenUrl, String userinfoUrl, String jsonpath, String clientID, String clientSecret, String callbackUrl) {

		settingService.set("sso_codeUrl", codeUrl);
		settingService.set("sso_tokenUrl", tokenUrl);
		settingService.set("sso_userinfoUrl", userinfoUrl);
		settingService.set("sso_jsonpath", jsonpath);
		settingService.set("sso_clientID", clientID);
		settingService.set("sso_clientSecret", clientSecret);
		settingService.set("sso_callbackUrl", callbackUrl);

		return renderSuccess();
	}

	@Mapping("redirect")
	public void redirect(Context ctx) {

		String codeUrl = settingService.get("sso_codeUrl");
		String clientID = settingService.get("sso_clientID");
		String callbackUrl = settingService.get("sso_callbackUrl");

		String url = codeUrl + "?client_id=" + clientID + "&response_type=code&redirect_uri=" + callbackUrl + "&oauth_timestamp=" + System.currentTimeMillis() + "&state=";

		ctx.redirect(url);
	}
	
	@Mapping("code")
	public void code(String code, Context ctx) {

		String tokenUrl = settingService.get("sso_tokenUrl");
		String userinfoUrl = settingService.get("sso_userinfoUrl");
		String jsonpath = settingService.get("sso_jsonpath");
		String clientID = settingService.get("sso_clientID");
		String clientSecret = settingService.get("sso_clientSecret");
		String callbackUrl = settingService.get("sso_callbackUrl");

		String getTokenUrl = tokenUrl + "?grant_type=authorization_code&oauth_timestamp=" + System.currentTimeMillis() + "&client_id=" + clientID + "&client_secret=" + clientSecret + "&code=" + code
				+ "&redirect_uri=" + callbackUrl;

		String post = HttpUtil.post(getTokenUrl, "");

		JSONObject entries = JSONUtil.parseObj(post);
		String accessToken = entries.getStr("access_token");
		String userInfoUrl = userinfoUrl + "?access_token=" + accessToken;
		String userinfoStr = HttpUtil.get(userInfoUrl);
		String read = JsonPath.read(userinfoStr, jsonpath);

		Admin admin = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq(Admin::getName, read), Admin.class);
		if (admin == null) {
			ctx.outputAsJson(JSONUtil.toJsonPrettyStr(renderError(m.get("ssoStr.userNotExist")))); 
			return;
		}

		admin.setAutoKey(UUID.randomUUID().toString()); // 生成自动登录code
		sqlHelper.updateById(admin);

		Context.current().sessionSet("localType", "local");
		Context.current().sessionSet("isLogin", true);
		Context.current().sessionSet("admin", admin);
		Context.current().sessionRemove("imgCode"); // 立刻销毁验证码

		ctx.redirect("/adminPage/monitor");
	}
}
