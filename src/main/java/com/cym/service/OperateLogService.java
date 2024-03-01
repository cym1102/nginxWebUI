package com.cym.service;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import com.cym.model.OperateLog;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

@Component
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
