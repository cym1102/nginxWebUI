package com.cym.service;

import org.noear.solon.annotation.Inject;
import org.noear.solon.aspect.annotation.Service;

import com.cym.sqlhelper.utils.SqlHelper;

@Service
public class LocationService {
	@Inject
	SqlHelper sqlHelper;
}
