package com.cym.sqlhelper.bean;

import com.cym.sqlhelper.bean.Sort.Direction;

public class Order {
	Direction direction;
	String column;

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

}
