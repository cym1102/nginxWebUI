package com.cym.controller.adminPage;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.cym.config.ScheduleTask;
import com.cym.model.Log;
import com.cym.service.LogService;
import com.cym.service.SettingService;
import com.cym.utils.BaseController;
import com.cym.utils.JsonResult;
import com.mysql.cj.xdevapi.JsonArray;

import cn.craccd.sqlHelper.bean.Page;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

@Controller
@RequestMapping("/adminPage/log")
public class LogController extends BaseController {
	@Autowired
	SettingService settingService;
	@Autowired
	LogService logService;
	@Autowired
	ScheduleTask scheduleTask;

	@RequestMapping("")
	public ModelAndView index(HttpSession httpSession, ModelAndView modelAndView, Page page) {
		page = logService.search(page);

		modelAndView.addObject("page", page);
		modelAndView.setViewName("/adminPage/log/index");
		return modelAndView;
	}

	@RequestMapping("del")
	@ResponseBody
	public JsonResult del(String id) {
		sqlHelper.deleteById(id, Log.class);
		return renderSuccess();
	}

	@RequestMapping("delAll")
	@ResponseBody
	public JsonResult delAll(String id) {
		sqlHelper.deleteByQuery(null, Log.class);
		return renderSuccess();
	}

	@RequestMapping("detail")
	@ResponseBody
	public JsonResult detail(String id) {
		Log log = sqlHelper.findById(id, Log.class);
		return renderSuccess(log);

	}

	@RequestMapping("detailByTIme")
	@ResponseBody
	public JsonResult detailByTIme(String startDate, String endDate) {
		List<Log> logAll = logService.findByDate(startDate, endDate);

		JSONObject jsonObjectFilter = new JSONObject();
		jsonObjectFilter.set("uv", new JsonArray());
		jsonObjectFilter.set("pv", new JsonArray());
		jsonObjectFilter.set("browser", new JsonArray());
		jsonObjectFilter.set("httpReferer", new JsonArray());
		jsonObjectFilter.set("status", new JsonArray());

		for (Log log : logAll) {
			JSONObject jsonObject = JSONUtil.parseObj(log.getJson());

			addToJsonArray(jsonObjectFilter.getJSONArray("uv"), jsonObject.getJSONArray("uv"));
			addToJsonArray(jsonObjectFilter.getJSONArray("pv"), jsonObject.getJSONArray("pv"));
			addToJsonArray(jsonObjectFilter.getJSONArray("browser"), jsonObject.getJSONArray("browser"));
			addToJsonArray(jsonObjectFilter.getJSONArray("httpReferer"), jsonObject.getJSONArray("httpReferer"));
			addToJsonArray(jsonObjectFilter.getJSONArray("status"), jsonObject.getJSONArray("status"));

		}

		return renderSuccess(jsonObjectFilter);

	}

	private void addToJsonArray(JSONArray jsonArrayFilter, JSONArray jsonArray) {
		for (int i = 0; i < jsonArray.size(); i++) {
			for (int j = 0; j < jsonArrayFilter.size(); j++) {
				if (jsonArray.getJSONObject(i).getStr("name").equals(jsonArrayFilter.getJSONObject(j).getStr("name"))) {
					Long count = jsonArray.getJSONObject(i).getLong("value") + jsonArrayFilter.getJSONObject(j).getLong("value");
					jsonArrayFilter.getJSONObject(i).set("value", count);
					return;
				}
			}

			JSONObject jsonObject = new JSONObject();
			jsonObject.set("name", jsonArray.getJSONObject(i).getStr("name"));
			jsonObject.set("value", jsonArray.getJSONObject(i).getStr("value"));
			jsonArrayFilter.add(jsonObject);
		}

	}

	@RequestMapping("analysis")
	@ResponseBody
	public JsonResult analysis() {
		scheduleTask.diviLog();
		return renderSuccess();

	}

}
