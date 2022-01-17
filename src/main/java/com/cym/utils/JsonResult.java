package com.cym.utils;

/**
 * 返回结果
 */
public class JsonResult<T> {
	/**
	 * 请求结果
	 */
	private boolean success;
	/**
	 * 请求状态 200:请求成功 401:token无效 500:服务器错误
	 */
	private String status;
	/**
	 * 错误信息
	 */
	private String msg;
	/**
	 * 返回内容
	 */
	private T obj;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public T getObj() {
		return obj;
	}

	public void setObj(T obj) {
		this.obj = obj;
	}

}
