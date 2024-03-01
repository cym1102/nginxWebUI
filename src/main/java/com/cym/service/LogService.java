package com.cym.service;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cym.model.Log;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.JdbcTemplate;
import com.cym.sqlhelper.utils.SqlHelper;

import cn.hutool.core.util.StrUtil;

@Component
public class LogService {
	@Inject
	SqlHelper sqlHelper;
	@Inject
	JdbcTemplate jdbcTemplate;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public boolean hasDir(String path, String id) {
		ConditionAndWrapper conditionAndWrapper = new ConditionAndWrapper().eq("path", path);
		if(StrUtil.isNotEmpty(id)) {
			conditionAndWrapper.ne("id", id);
		}
		return sqlHelper.findCountByQuery(conditionAndWrapper, Log.class) > 0;

	}

	public Page search(Page page) {
		return sqlHelper.findPage(page, Log.class);
	}
	


}
