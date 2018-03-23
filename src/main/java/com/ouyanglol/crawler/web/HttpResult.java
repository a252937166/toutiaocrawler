package com.ouyanglol.crawler.web;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel
public class HttpResult<T> implements Serializable {

	private static final long serialVersionUID = 7475582465876485316L;
	
	public static final Integer CODE_SUCCESS = 200;

	@ApiModelProperty(value="状态码")
	private Integer code = CODE_SUCCESS;
	
	@ApiModelProperty(value="状态",allowableValues="true,false")
	private boolean success = true;
	
	@ApiModelProperty(value="消息")
	private String msg;
	
	@JsonInclude(Include.NON_NULL)
	@ApiModelProperty(value="数据")
	private T result;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	
}