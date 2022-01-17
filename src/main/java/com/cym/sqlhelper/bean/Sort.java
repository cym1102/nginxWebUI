package com.cym.sqlhelper.bean;

import java.util.ArrayList;
import java.util.List;

import com.cym.sqlhelper.reflection.ReflectionUtil;
import com.cym.sqlhelper.reflection.SerializableFunction;

import cn.hutool.core.util.StrUtil;

public class Sort {
	List<Order> orderList = new ArrayList<>();

	public static enum Direction {
		ASC, DESC;
	}

	public Sort() {

	}

	public Sort(String column, Direction direction) {
		Order order = new Order();
		order.setColumn(column);
		order.setDirection(direction);

		orderList.add(order);
	}

	public Sort(List<Order> orderList) {
		this.orderList.addAll(orderList);
	}

	public <T, R> Sort(SerializableFunction<T, R> column, Direction direction) {
		Order order = new Order();
		order.setColumn(ReflectionUtil.getFieldName(column));
		order.setDirection(direction);

		orderList.add(order);
	}

	public Sort add(String column, Direction direction) {
		Order order = new Order();
		order.setColumn(column);
		order.setDirection(direction);

		orderList.add(order);
		
		return this;
	}
	
	public <T, R> Sort add(SerializableFunction<T, R> column, Direction direction) {
		Order order = new Order();
		order.setColumn(ReflectionUtil.getFieldName(column));
		order.setDirection(direction);

		orderList.add(order);
		
		return this;
	}

	public String toString() {
		List<String> sqlList = new ArrayList<>();
		for (Order order : orderList) {

			String sql = StrUtil.toUnderlineCase(order.getColumn());

			if (order.getDirection() == Direction.ASC) {
				sql += " ASC";
			}
			if (order.getDirection() == Direction.DESC) {
				sql += " DESC";
			}

			sqlList.add(sql);
		}

		return " ORDER BY " + StrUtil.join(",", sqlList);
	}


	
}
