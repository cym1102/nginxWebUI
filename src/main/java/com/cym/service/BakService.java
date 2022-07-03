package com.cym.service;

import java.util.List;

import org.noear.solon.annotation.Inject;
import org.noear.solon.aspect.annotation.Service;

import com.cym.model.Bak;
import com.cym.model.BakSub;
import com.cym.sqlhelper.bean.Page;
import com.cym.sqlhelper.bean.Sort;
import com.cym.sqlhelper.bean.Sort.Direction;
import com.cym.sqlhelper.utils.ConditionAndWrapper;
import com.cym.sqlhelper.utils.SqlHelper;

@Service
public class BakService {
	@Inject
	SqlHelper sqlHelper;

	public Page<Bak> getList(Page page) {
		return sqlHelper.findPage(new ConditionAndWrapper(), new Sort(Bak::getTime, Direction.DESC), page, Bak.class);
	}

	public List<BakSub> getSubList(String id) {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq(BakSub::getBakId, id), BakSub.class);
	}

	public void del(String id) {
		sqlHelper.deleteById(id, Bak.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(BakSub::getBakId, id), BakSub.class);
	}

	public void delAll() {
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), Bak.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), BakSub.class);
	}

	public Bak getPre(String id) {
		Bak bak = sqlHelper.findById(id, Bak.class);
		Bak pre = sqlHelper.findOneByQuery(new ConditionAndWrapper().lt(Bak::getTime, bak.getTime()), new Sort(Bak::getTime, Direction.DESC), Bak.class);

		return pre;
	}

}
