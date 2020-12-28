package com.cym.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Http;

import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;

@Service
public class HttpService {
	@Autowired
	SqlHelper sqlHelper;

	public List<Http> findAll() {
		return sqlHelper.findAll(new Sort("seq + 0", Direction.ASC), Http.class);
	}

	public void setAll(List<Http> https) {
		Http logFormat = null;
		Http accessLog = null;
		for (Http http : https) {
			if (http.getName().equals("log_format")) {
				logFormat = http;
			}
			if (http.getName().equals("access_log")) {
				accessLog = http;
			}
		}
		if (logFormat != null) {
			https.remove(logFormat);
			https.add(logFormat);
		}
		if (accessLog != null) {
			https.remove(accessLog);
			https.add(accessLog);
		}

		for (Http http : https) {
			Http httpOrg = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq("name", http.getName()), Http.class);

			if (httpOrg != null) {
				http.setId(httpOrg.getId());
			}

			http.setSeq(buildOrder());
			http.setValue(http.getValue() + http.getUnit());

			sqlHelper.insertOrUpdate(http);

		}

	}

	public void setSeq(String httpId, Integer seqAdd) {
		Http http = sqlHelper.findById(httpId, Http.class);

		List<Http> httpList = sqlHelper.findAll(new Sort("seq + 0", Direction.ASC), Http.class);
		if (httpList.size() > 0) {
			Http tagert = null;
			if (seqAdd < 0) {
				// 上移
				for (int i = 0; i < httpList.size(); i++) {
					if (httpList.get(i).getSeq() < http.getSeq()) {
						tagert = httpList.get(i);
					}
				}
			} else {
				// 下移
				for (int i = httpList.size() - 1; i >= 0; i--) {
					if (httpList.get(i).getSeq() > http.getSeq()) {
						tagert = httpList.get(i);
					}
				}
			}

			if (tagert != null) {
				// 交换seq
				Long seq = tagert.getSeq();
				tagert.setSeq(http.getSeq());
				http.setSeq(seq);

				sqlHelper.updateById(tagert);
				sqlHelper.updateById(http);
			}

		}

	}

	public Long buildOrder() {

		Http http = sqlHelper.findOneByQuery(new Sort("seq + 0", Direction.DESC), Http.class);
		if (http != null) {
			return http.getSeq() + 1;
		}

		return 0l;
	}

	public Http getName(String name) {
		Http http = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq("name", name), Http.class);

		return http;
	}

}
