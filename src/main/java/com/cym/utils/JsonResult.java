package com.cym.utils;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Author: D.Yang Email: koyangslash@gmail.com Date: 16/8/31 Time: 下午5:50
 * Describe: 封装Json返回信息
 */
@ApiModel("返回结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonResult<T> {
	@ApiModelProperty("请求结果")
	private boolean success;
	@ApiModelProperty("请求状态 200:请求成功 401:token无效 500:服务器错误")
	private String status;
	@ApiModelProperty("错误信息")
	private String msg;
	@ApiModelProperty("返回内容")
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
