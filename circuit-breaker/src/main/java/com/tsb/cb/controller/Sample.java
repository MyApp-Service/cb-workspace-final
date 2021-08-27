package com.tsb.cb.controller;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.tsb.cb.annotation.TSBCircuitBreaker;

@Component
public class Sample {

	@TSBCircuitBreaker(name="verifyLogin",fallback = "fallbackMethod")
	public String forTestmethod(boolean isExp) {
		
		if(isExp) {
			throw new RuntimeException();
		}
		
		return "SUCCESS";
	}
	
	public String fallbackMethod(Throwable t) {
		System.out.println("Calling fallback...");
		return "FALLBACK";
	}
}
