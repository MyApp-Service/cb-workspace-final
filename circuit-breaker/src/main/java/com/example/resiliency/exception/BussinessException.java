package com.example.resiliency.exception;

import org.springframework.web.client.HttpServerErrorException;

public class BussinessException extends RuntimeException{
	
	private String msg;
	
	public BussinessException(String msg) {
		// TODO Auto-generated constructor stub
		super(msg);
		this.msg = msg;
	}

	
	//HttpServerErrorException
	
	
}
