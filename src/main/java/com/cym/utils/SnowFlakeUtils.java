package com.cym.utils;

import com.cym.sqlhelper.utils.SnowFlake;

public class SnowFlakeUtils {
	static SnowFlake snowFlake = new SnowFlake(1, 1);

	public static Long getId() {
		return Long.parseLong(snowFlake.nextId());
	}
	
	public static void main(String[] args) {
		
		System.err.println(getId());
		System.err.println(getId());
		System.err.println(getId());
	}

}
