package com.cym.controller.adminPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.ModelAndView;

import com.cym.model.DenyAllow;
import com.cym.model.Http;
import com.cym.service.HttpService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.cym.utils.SnowFlakeUtils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Controller
@Mapping("/adminPage/http")
public class HttpController extends BaseController {
	@Inject
	HttpService httpService;
	@Inject
	SettingService settingService;

	@Mapping("")
	public ModelAndView index(ModelAndView modelAndView) {
		List<Http> httpList = httpService.findAll();

		modelAndView.put("httpList", httpList);
		modelAndView.put("denyAllowList", sqlHelper.findAll(DenyAllow.class));

		modelAndView.view("/adminPage/http/index.html");
		return modelAndView;
	}

	@Mapping("addOver")
	public JsonResult addOver(Http http) {
		if (StrUtil.isEmpty(http.getId())) {
			http.setSeq(SnowFlakeUtils.getId());
		}
		sqlHelper.insertOrUpdate(http);

		return renderSuccess();
	}

	@Mapping("addTemplate")
	public JsonResult addTemplate(String templateId) {
		httpService.addTemplate(templateId);

		return renderSuccess();
	}

	@Mapping("detail")
	public JsonResult detail(String id) {
		return renderSuccess(sqlHelper.findById(id, Http.class));
	}

	@Mapping("del")
	public JsonResult del(String id) {
		String[] ids = id.split(",");
		sqlHelper.deleteByIds(ids, Http.class);

		return renderSuccess();
	}

	@Mapping("addGiudeOver")
	public JsonResult addGiudeOver(String json, Boolean logStatus, Boolean webSocket, Boolean mimeTypes) {
		List<Http> https = JSONUtil.toList(JSONUtil.parseArray(json), Http.class);

		if (mimeTypes) {
			Http http = new Http();
			http.setName("include");
			http.setValue("mime.types");
			http.setUnit("");
			https.add(http);

			http = new Http();
			http.setName("default_type");
			http.setValue("application/octet-stream");
			http.setUnit("");
			https.add(http);
		}

		if (logStatus) {
			Http http = new Http();
			http.setName("access_log");
			http.setValue(homeConfig.home + "log/access.log");
			http.setUnit("");
			https.add(http);

			http = new Http();
			http.setName("error_log");
			http.setValue(homeConfig.home + "log/error.log");
			http.setUnit("");
			https.add(http);
		}

		if (webSocket) {
			Http http = new Http();
			http.setName("map");
			http.setValue("$http_upgrade $connection_upgrade {\r\n" //
					+ "    default upgrade;\r\n" //
					+ "    '' close;\r\n" + "}\r\n");//
			http.setUnit("");
			https.add(http);
		}

		httpService.setAll(https);

		return renderSuccess();
	}

	@Mapping("setOrder")
	public JsonResult setOrder(String id, Integer count) {
		httpService.setSeq(id, count);
		return renderSuccess();
	}

	@Mapping("getDenyAllow")
	public JsonResult getDenyAllow() {

		Map<String, String> map = new HashMap<>();
		map.put("denyAllow", settingService.get("denyAllow"));
		map.put("denyId", settingService.get("denyId"));
		map.put("allowId", settingService.get("allowId"));

		return renderSuccess(map);
	}

	@Mapping("setDenyAllow")
	public JsonResult setDenyAllow(String denyAllow, String denyId, String allowId) {

		settingService.set("denyAllow", denyAllow);
		settingService.set("denyId", denyId);
		settingService.set("allowId", allowId);

		return renderSuccess();
	}

	@Mapping("setEnable")
	public JsonResult setEnable(Http http) {
		sqlHelper.updateById(http);
		return renderSuccess();
	}
}
