package com.cym.service;

import org.noear.solon.annotation.Inject;
import org.noear.solon.aspect.annotation.Service;

import com.cym.model.OperateLog;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

@Service
public class OperateLogService {
	@Inject
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
