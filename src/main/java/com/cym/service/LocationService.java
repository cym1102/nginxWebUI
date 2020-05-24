package com.cym.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.craccd.sqlHelper.utils.SqlHelper;

@Service
public class LocationService {
	@Autowired
	SqlHelper sqlHelper;
}
