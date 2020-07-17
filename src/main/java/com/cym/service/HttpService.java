package com.cym.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Http;
import com.cym.model.Server;
import com.cym.model.Stream;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;
import cn.hutool.core.util.StrUtil;

@Service
public class HttpService {
	@Autowired
	SqlHelper sqlHelper;

//	public boolean hasName(String name) {
//		return sqlHelper.findCountByQuery(new ConditionAndWrapper().eq("name", name), Http.class) > 0;
//	}

	public void setAll(List<Http> https) {
		for (Http http : https) {
			Http httpOrg = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq("name", http.getName()), Http.class);

			if (httpOrg != null) {
				http.setId(httpOrg.getId());
			}

			http.setValue(http.getValue() + http.getUnit());
			

			sqlHelper.insertOrUpdate(http);

		}

	}

	public void setSeq(String httpId, Integer seqAdd) {
		Http http = sqlHelper.findById(httpId, Http.class);

		List<Http> httpList = sqlHelper.findAll(new Sort("seq", Direction.ASC), Http.class);
		if (httpList.size() > 0) {
			Http tagert = null;
			if (seqAdd < 0) {
				for (int i = 0; i < httpList.size(); i++) {
					if (httpList.get(i).getSeq() < http.getSeq()) {
						tagert = httpList.get(i);
					}
				}
			} else {
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

		Http http = sqlHelper.findOneByQuery(new Sort("seq", Direction.DESC), Http.class);
		if (http != null) {
			return http.getSeq() + 1;
		}

		return 0l;
	}

}
