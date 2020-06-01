package com.cym.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cym.model.Upstream;
import com.cym.model.UpstreamServer;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.ConditionOrWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;

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
	public void addOver(Upstream upstream, String[] servers, Integer[] ports, Integer[] weights, Integer[] maxFails, Integer[] failTimeout, String[] status) {
		if (upstream.getProxyType() == 1 || upstream.getTactics() == null) {
			upstream.setTactics("");
		}

		sqlHelper.insertOrUpdate(upstream);

		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq("upstreamId", upstream.getId()), UpstreamServer.class);

		if (servers != null) {
			for (int i = 0; i < servers.length; i++) {
				UpstreamServer upstreamServer = new UpstreamServer();
				upstreamServer.setUpstreamId(upstream.getId());
				upstreamServer.setServer(servers[i]);
				upstreamServer.setPort(ports[i]);
				upstreamServer.setWeight(weights[i]);

				upstreamServer.setMaxFails(maxFails[i]);
				upstreamServer.setFailTimeout(failTimeout[i]);
				upstreamServer.setStatus(status[i]);

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

}
