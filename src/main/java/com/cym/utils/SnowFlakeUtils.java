package com.cym.utils;

import com.cym.sqlhelper.utils.SnowFlake;

public class SnowFlakeUtils {
	static SnowFlake snowFlake = new SnowFlake(1, 1);

	public static Long getId() {
		return Long.parseLong(snowFlake.nextId());
	}

}
