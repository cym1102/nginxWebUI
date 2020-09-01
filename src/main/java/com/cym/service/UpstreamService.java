package com.cym.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cym.model.Param;
import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.ConditionOrWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

@Service
public class UpstreamService {
	@Autowired
	SqlHelper sqlHelper;

	public Page search(Page page, String word) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper();

		if (StrUtil.isNotEmpty(word)) {
			conditionAndWrapper.and(new ConditionOrWrapper().like("name", word));
		}

		page = sqlHelper.findPage(conditionAndWrapper, new Sort("id", Direction.DESC), page, Upstream.class);

		return page;
	}

	@Transactional
	public void deleteById(String id) {
		sqlHelper.deleteById(id, Upstream.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("upstreamId", id), UpstreamServer.class);
	}

	@Transactional
	public void addOver(Upstream upstream,List<UpstreamServer> upstreamServers, String upstreamParamJson) {
		if (upstream.getProxyType() == 1 || upstream.getTactics() == null) {
			upstream.setTactics("");
		}

		sqlHelper.insertOrUpdate(upstream);
		
		List<Param> paramList = new ArrayList<Param>();
		if (StrUtil.isNotEmpty(upstreamParamJson) && JSONUtil.isJson(upstreamParamJson)) {
			paramList = JSONUtil.toList(JSONUtil.parseArray(upstreamParamJson), Param.class);
		}
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("upstreamId", upstream.getId()), Param.class);
		 // 反向插入,保证列表与输入框对应
		Collections.reverse(paramList);
		for (Param param : paramList) {
			param.setUpstreamId(upstream.getId());
			sqlHelper.insert(param);
		}
		

		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("upstreamId", upstream.getId()), UpstreamServer.class);
		if (upstreamServers != null) {
			 // 反向插入,保证列表与输入框对应
			Collections.reverse(upstreamServers);
			
			for (UpstreamServer upstreamServer :upstreamServers) {
				upstreamServer.setUpstreamId(upstream.getId());
				sqlHelper.insert(upstreamServer);
			}
		}

	}

	public List<UpstreamServer> getUpstreamServers(String id) {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq("upstreamId", id), UpstreamServer.class);
	}

	@Transactional
	public void del(String id) {
		sqlHelper.deleteById(id, Upstream.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("upstreamId", id), UpstreamServer.class);

	}

	public List<Upstream> getListByProxyType(Integer proxyType) {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq("proxyType", proxyType), Upstream.class);
	}

	public Long getCountByName(String name) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("name", name), Upstream.class);
	}
	public Long getCountByNameWithOutId(String name, String id) {
		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("name", name).ne("id", id), Upstream.class);
	}

	public List<UpstreamServer> getServerListByMonitor(int monitor) {
		List<String> upstreamIds = sqlHelper.findIdsByQuery(new ConditionAndWrapper().eq("monitor", monitor), Upstream.class);
		
		return sqlHelper.findListByQuery(new ConditionAndWrapper().in("upstreamId", upstreamIds), UpstreamServer.class);
	}

	public List<UpstreamServer> getAllServer() {
		return sqlHelper.findAll(UpstreamServer.class);
	}




}
