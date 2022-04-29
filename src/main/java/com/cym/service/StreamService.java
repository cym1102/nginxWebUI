package com.cym.service;

import java.util.List;

import org.noear.solon.annotation.Inject;
import org.noear.solon.extend.aspect.annotation.Service;

import com.cym.model.Http;
import com.cym.model.Param;
import com.cym.model.Stream;
import com.cym.sqlhelper.bean.Sort;
import com.cym.sqlhelper.bean.Sort.Direction;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;
import com.cym.utils.SnowFlakeUtils;

@Service
public class StreamService {
	@Inject
	SqlHelper sqlHelper;

	public void setSeq(String streamId, Integer seqAdd) {
		Stream http = sqlHelper.findById(streamId, Stream.class);

		List<Stream> httpList = sqlHelper.findAll(new Sort("seq", Direction.ASC), Stream.class);
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

	public List<Stream> findAll() {
		return sqlHelper.findAll(new Sort("seq", Direction.ASC), Stream.class);
	}

	public void addTemplate(String templateId) {
		List<Param> parmList = sqlHelper.findListByQuery(new ConditionAndWrapper().eq(Param::getTemplateId, templateId), Param.class);

		for (Param param : parmList) {
			Stream stream = new Stream();
			stream.setName(param.getName());
			stream.setValue(param.getValue());
			stream.setSeq(SnowFlakeUtils.getId());

			sqlHelper.insert(stream);
		}

	}

	public void setAll(List<Stream> streams) {
		for (Stream stream : streams) {
			Stream streamOrg = sqlHelper.findOneByQuery(new ConditionAndWrapper().eq("name", stream.getName()), Stream.class);

			if (streamOrg != null) {
				sqlHelper.deleteById(streamOrg.getId(), Stream.class);
			}
			
			sqlHelper.insert(stream);
		}

	}

}
