package br.ufpb.dcx.apps4society.educapi.resources.exceptions;

import java.io.Serializable;

public class StandardError implements Serializable{
	private static final long serialVersionUID = 1L;
	private Integer status;
	private String msg;
	private Long timeStamp;
	private String path;

	public StandardError(Integer status, String msg, Long timeStamp, String path) {
		this.status = status;
		this.msg = msg;
		this.timeStamp = timeStamp;
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	
}
