package com.cym.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cym.model.Group;
import com.cym.model.Remote;

import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.ConditionOrWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;

@Service
public class GroupService {
	@Autowired
	SqlHelper sqlHelper;

	@Transactional
	public void delete(String id) {

		sqlHelper.deleteById(id, Group.class);

		List<Remote> remoteList = sqlHelper.findListByQuery(new ConditionAndWrapper().eq("parentId", id), Remote.class);
		for (Remote remote : remoteList) {
			remote.setParentId(null);
			sqlHelper.updateAllColumnById(remote);
		}

		List<Group> groupList = sqlHelper.findListByQuery(new ConditionAndWrapper().eq("parentId", id), Group.class);
		for (Group group : groupList) {
			group.setParentId(null);
			sqlHelper.updateAllColumnById(group);
		}

	}

	public List<Group> getListByParent(String id) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper();
		if (StrUtil.isEmpty(id)) {
			conditionAndWrapper.and(new ConditionOrWrapper().eq("parentId", "").isNull("parentId"));
		} else {
			conditionAndWrapper.eq("parentId", id);
		}

		return sqlHelper.findListByQuery(conditionAndWrapper, Group.class);
	}

}
