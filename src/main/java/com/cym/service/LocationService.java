package com.cym.service;

import org.noear.solon.annotation.Inject;
import org.noear.solon.extend.aspect.annotation.Service;

import com.cym.sqlhelper.utils.SqlHelper;

@Service
public class LocationService {
	@Inject
	SqlHelper sqlHelper;
}
