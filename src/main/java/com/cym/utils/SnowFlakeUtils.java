package com.cym.utils;

import cn.craccd.sqlHelper.utils.SnowFlake;

public class SnowFlakeUtils {
	static SnowFlake snowFlake = new SnowFlake(1, 1);

	public static Long getId() {
		return Long.parseLong(snowFlake.nextId());
	}

}
