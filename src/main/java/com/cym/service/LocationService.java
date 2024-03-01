package com.cym.service;

import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import com.cym.sqlhelper.utils.SqlHelper;

@Component
public class LocationService {
	@Inject
	SqlHelper sqlHelper;
}
