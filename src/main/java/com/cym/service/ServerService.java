package com.cym.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cym.model.Location;
import com.cym.model.Param;
import com.cym.model.Server;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.ConditionOrWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Service
public class ServerService {
	@Autowired
	SqlHelper sqlHelper;

	public Page search(Page page, String sortColum, String direction) {
		Sort sort = null;
		if (StrUtil.isNotEmpty(sortColum)) {
			sort = new Sort(sortColum, "asc".equalsIgnoreCase(direction) ? Direction.ASC : Direction.DESC);
		}

		page = sqlHelper.findPage(sort, page, Server.class);

		return page;
	}

	@Transactional
	public void deleteById(String id) {
		sqlHelper.deleteById(id, Server.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("serverId", id), Location.class);
	}

	public List<Location> getLocationByServerId(String serverId) {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq("serverId", serverId), Location.class);
	}

	@Transactional
	public void addOver(Server server, String serverParamJson, Integer[] type, String[] path, String[] value, String[] upstreamId, String[] upstreamPath, String[] locationParamJson) {
		sqlHelper.insertOrUpdate(server);
		List<Param> paramList = new ArrayList<Param>();
		if (StrUtil.isNotEmpty(serverParamJson) && JSONUtil.isJson(serverParamJson.replace("%2C", ","))) {
			paramList = JSONUtil.toList(JSONUtil.parseArray(serverParamJson.replace("%2C", ",")), Param.class);
		}
		List<String> locationIds = sqlHelper.findIdsByQuery(new ConditionAndWrapper().eq("serverId", server.getId()), Location.class);
		sqlHelper.deleteByQuery(new ConditionOrWrapper().eq("serverId", server.getId()).in("locationId", locationIds), Param.class);
		for (Param param : paramList) {
			param.setServerId(server.getId());
			sqlHelper.insert(param);
		}

		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("serverId", server.getId()), Location.class);

		if (type != null) {
			for (int i = 0; i < type.length; i++) {
				Location location = new Location();
				location.setServerId(server.getId());
				location.setType(type[i]);
				location.setPath(path[i]);

				if (location.getType() == 0 || location.getType() == 1) {
					location.setValue(value[i]);
				} else if (location.getType() == 2) {
					location.setUpstreamId(upstreamId[i]);

					if (!upstreamPath[i].equals("is_null")) {
						location.setUpstreamPath(upstreamPath[i]);
					}
				}

				sqlHelper.insert(location);

				paramList = new ArrayList<Param>();
				if (locationParamJson.length > 0 && StrUtil.isNotEmpty(locationParamJson[i]) && JSONUtil.isJson(locationParamJson[i].replace("%2C", ","))) {
					paramList = JSONUtil.toList(JSONUtil.parseArray(locationParamJson[i].replace("%2C", ",")), Param.class);
				}

				for (Param param : paramList) {
					param.setLocationId(location.getId());
					sqlHelper.insert(param);
				}
			}
		}
	}

	@Transactional
	public void addOverTcp(Server server, String serverParamJson) {
		sqlHelper.insertOrUpdate(server);

		List<String> locationIds = sqlHelper.findIdsByQuery(new ConditionAndWrapper().eq("serverId", server.getId()), Location.class);
		sqlHelper.deleteByQuery(new ConditionOrWrapper().eq("serverId", server.getId()).in("locationId", locationIds), Param.class);
		List<Param> paramList = new ArrayList<Param>();
		if (StrUtil.isNotEmpty(serverParamJson) && JSONUtil.isJson(serverParamJson.replace("%2C", ","))) {
			paramList = JSONUtil.toList(JSONUtil.parseArray(serverParamJson.replace("%2C", ",")), Param.class);
		}

		for (Param param : paramList) {
			param.setServerId(server.getId());
			sqlHelper.insert(param);
		}

		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("serverId", server.getId()), Location.class);
	}

	public List<Server> getListByProxyType(Integer proxyType) {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq("proxyType", proxyType), Server.class);
	}

	@Transactional
	public void clone(String id) {
		Server server = sqlHelper.findById(id, Server.class);

		List<Location> locations = sqlHelper.findListByQuery(new ConditionAndWrapper().eq("serverId", server.getId()), Location.class);
		List<Param> params = sqlHelper.findListByQuery(new ConditionAndWrapper().eq("serverId", server.getId()), Param.class);

		server.setId(null);
		sqlHelper.insertOrUpdate(server);
		for (Param param : params) {
			param.setId(null);
			param.setServerId(server.getId());
			sqlHelper.insert(param);
		}
		
		
		for (Location location : locations) {
			params = sqlHelper.findListByQuery(new ConditionAndWrapper().eq("locationId", location.getId()), Param.class);
			
			location.setId(null);
			location.setServerId(server.getId());
			sqlHelper.insert(location);
			
			for (Param param : params) {
				param.setId(null);
				param.setLocationId(location.getId());
				sqlHelper.insert(param);
			}
		}

		
	}

}
