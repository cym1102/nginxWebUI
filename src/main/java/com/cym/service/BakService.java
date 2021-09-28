package com.cym.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cym.model.Bak;
import com.cym.model.BakSub;

import cn.craccd.sqlHelper.bean.Page;
import cn.craccd.sqlHelper.bean.Sort;
import cn.craccd.sqlHelper.bean.Sort.Direction;
import cn.craccd.sqlHelper.utils.ConditionAndWrapper;
import cn.craccd.sqlHelper.utils.SqlHelper;

@Service
public class BakService {
	@Autowired
	SqlHelper sqlHelper;

	public Page<Bak> getList(Page page) {
		return sqlHelper.findPage(new ConditionAndWrapper(), new Sort(Bak::getTime, Direction.DESC), page, Bak.class);
	}

	public List<BakSub> getSubList(String id) {
		return sqlHelper.findListByQuery(new ConditionAndWrapper().eq(BakSub::getBakId, id),  BakSub.class);
	}

	public void del(String id) {
		sqlHelper.deleteById(id, Bak.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper().eq(BakSub::getBakId, id), BakSub.class);
	}
	
	public void delAll() {
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), Bak.class);
		sqlHelper.deleteByQuery(new ConditionAndWrapper(), BakSub.class);
	}

	
}
