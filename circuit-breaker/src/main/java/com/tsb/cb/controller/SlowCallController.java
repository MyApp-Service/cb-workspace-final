package com.tsb.cb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.tsb.cb.annotation.TSBCircuitBreaker;
import com.tsb.cb.service.Resilience4jProperties;

@RestController
@RequestMapping("apis")
public class SlowCallController {
	
	@Autowired
	RestTemplate template;
	
	@Autowired
	Resilience4jProperties properties;
	
	int count =0;
	
	
	@TSBCircuitBreaker(name="slowResponse", fallback = "fallbackService")
	@RequestMapping("slow/service/")
	public ResponseEntity<String> slowResponse() {
		
		//System.out.println(properties.getInstances());
		
		//String response = template.getForObject("http://localhost:8081/info", String.class);
		
		//System.out.println("From response::--"+response);
		System.out.println("Calling slowResponse");
		if(count>4 && count<10) {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		System.out.println("Count Value:--"+ count);
		count++;
		if(count>7 && count<11) {
			throw new RuntimeException("Its from Failure.....");
		}
		//String response = template.getForObject("http://localhost:8081/info", String.class);
		//System.out.println("It calls after thread");
		
		return ResponseEntity.ok().body("Its from slow response......");
	}
	
	public ResponseEntity<String> fallbackService(Throwable e){
		System.out.println("From fallbackService:---"+e.getMessage());
		return ResponseEntity.ok().body("Success, issue is there, its from fallback slow Response");
	}

}
