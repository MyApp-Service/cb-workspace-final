package com.tsb.cb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.tsb.cb.annotation.TSBCircuitBreaker;

@RestController
@RequestMapping("api1")
public class SampleRestController {
	@Autowired
	RestTemplate template;
	
	@TSBCircuitBreaker(name = "confirmLogin",fallback = "showMethodFallback")
	@RequestMapping("show")
	public String showMethod() {
		
		/*
		 * if(true) { throw new RuntimeException("Error from showMethod"); }
		 
		 */
		String response = template.getForObject("http://localhost:8081/info", String.class);
		
		return "Hi From showMethod";
	}
	
	public String showMethodFallback(Throwable e) {
		System.out.println("From showMethodFallback:---"+e.getMessage());
		return "Hi From showMethodFallback";
	}

}
