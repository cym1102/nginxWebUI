package com.cym.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Basic;
import com.cym.model.Stream;

import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.craccd.sqlHelper.utils.SqlHelper;

@Service
public class StreamService {
	@Autowired
	SqlHelper sqlHelper;

	public void setSeq(String streamId, Integer seqAdd) {
		Stream http = sqlHelper.findById(streamId, Stream.class);

		List<Stream> httpList = sqlHelper.findAll(new Sort("seq + 0", Direction.ASC), Stream.class);
		if (httpList.size() > 0) {
			Stream tagert = null;
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

		Stream stream = sqlHelper.findOneByQuery(new Sort("seq + 0", Direction.DESC), Stream.class);
		if (stream != null) {
			return stream.getSeq() + 1;
		}

		return 0l;
	}

	public List<Stream> findAll() {
		return sqlHelper.findAll(new Sort("seq + 0", Direction.ASC), Stream.class);
	}

	
	
}
