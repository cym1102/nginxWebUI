package com.cym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.OperateLog;
import com.cym.model.Server;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;

@Service
public class OperateLogService {
	@Autowired
	SqlHelper sqlHelper;

	public Page search(Page page) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper();

		page = sqlHelper.findPage(conditionAndWrapper, page, OperateLog.class);

		return page;
	}

	public void addLog(String beforeConf, String afterConf, String adminName) {
		OperateLog operateLog = new OperateLog();
		operateLog.setAdminName(adminName);
		operateLog.setBeforeConf(beforeConf);
		operateLog.setAfterConf(afterConf);

		sqlHelper.insert(operateLog);
	}

}
