package com.cym.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cym.model.Group;
import com.cym.model.Remote;

import cn.craccd.sqlHelper.utils.CriteriaAndWrapper;
import cn.craccd.sqlHelper.utils.CriteriaOrWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;

@Service
public class GroupService {
	@Autowired
	SqlHelper sqlHelper;

	@Transactional
	public void delete(String id) {

		sqlHelper.deleteById(id, Group.class);

		List<Remote> remoteList = sqlHelper.findListByQuery(new CriteriaAndWrapper().eq("parentId", id), Remote.class);
		for (Remote remote : remoteList) {
			remote.setParentId(null);
			sqlHelper.updateAllColumnById(remote);
		}

		List<Group> groupList = sqlHelper.findListByQuery(new CriteriaAndWrapper().eq("parentId", id), Group.class);
		for (Group group : groupList) {
			group.setParentId(null);
			sqlHelper.updateAllColumnById(group);
		}

	}

	public List<Group> getListByParent(String id) {
		CriteriaAndWrapper criteriaAndWrapper = new CriteriaAndWrapper();
		if (StrUtil.isEmpty(id)) {
			criteriaAndWrapper.and(new CriteriaOrWrapper().eq("parentId", "").isNull("parentId"));
		} else {
			criteriaAndWrapper.eq("parentId", id);
		}

		return sqlHelper.findListByQuery(criteriaAndWrapper, Group.class);
	}

}
