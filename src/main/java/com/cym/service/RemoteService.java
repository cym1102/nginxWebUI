package com.cym.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Admin;
import com.cym.model.AdminGroup;
import com.cym.model.Group;
import com.cym.model.Remote;

import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.ConditionOrWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;

@Service
public class RemoteService {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	SqlHelper sqlHelper;
	@Autowired
	AdminService adminService;

	public void getCreditKey(Remote remote, String code, String auth) {
		Map<String, Object> paramMap = new HashMap<String, Object>();

		paramMap.put("name", remote.getName());
		paramMap.put("pass", remote.getPass());
		paramMap.put("code", code);
		paramMap.put("auth", auth);
		try {
			String rs = HttpUtil.post(remote.getProtocol() + "://" + remote.getIp() + ":" + remote.getPort()
					+ "/adminPage/login/getCredit", paramMap, 2000);

			if (StrUtil.isNotEmpty(rs)) {
				JSONObject jsonObject = new JSONObject(rs);
				if (jsonObject.getBool("success")) {
					remote.setSystem(jsonObject.getJSONObject("obj").getStr("system"));
					remote.setCreditKey(jsonObject.getJSONObject("obj").getStr("creditKey"));
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public List<Remote> getBySystem(String system) {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq("system", system), Remote.class);
	}

	public List<Remote> getListByParent(String parentId) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper();
		if (StrUtil.isEmpty(parentId)) {
			conditionAndWrapper.and(new ConditionOrWrapper().eq("parentId", "").isNull("parentId"));
		} else {
			conditionAndWrapper.eq("parentId", parentId);
		}

		return sqlHelper.findListByQuery(conditionAndWrapper, Remote.class);
	}

	public List<Remote> getMonitorRemoteList() {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq("monitor", 1), Remote.class);
	}

	public boolean hasSame(Remote remote) {
		Long count = 0l;
		if (StrUtil.isEmpty(remote.getId())) {
			count = sqlHelper.findCountByQuery(
					new ConditionAndWrapper().eq("ip", remote.getIp()).eq("port", remote.getPort()), Remote.class);
		} else {
			count = sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("ip", remote.getIp())
					.eq("port", remote.getPort()).ne("id", remote.getId()), Remote.class);
		}

		if (count > 0) {
			return true;
		} else {
			return false;
		}
	}

	public List<Group> getGroupByAdmin(Admin admin) {
		if (admin.getType() == 0) {
			return sqlHelper.findAll(Group.class);
		} else {
			List<String> groupIds = adminService.getGroupIds(admin.getId());
			return sqlHelper.findListByIds(groupIds, Group.class);
		}

	}

}
