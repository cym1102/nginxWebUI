package com.cym.sqlhelper.utils;

public class Condition {
	public Condition(String column, String operation, Object value) {
		this.column = column;
		this.operation = operation;
		this.value = value;
	}

	String column;
	String operation;
	Object value;


	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
