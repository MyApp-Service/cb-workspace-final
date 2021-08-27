package com.tsb.cb.controller;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.tsb.cb.annotation.TSBCircuitBreaker;
import com.tsb.cb.annotation.TSBRetry;
import com.tsb.cb.annotation.TSBTimeLimiter;
import com.tsb.cb.config.CircuitBreakerProperties;
import com.tsb.cb.service.Resilience4jProperties;

@RestController
@RequestMapping("api")
public class Resilience4jController {
	
	@Autowired
	RestTemplate template;
	
	@Autowired
	Resilience4jProperties properties;
	
	@Autowired
	CircuitBreakerProperties prop;
	
	int count =0;
	
	@TSBCircuitBreaker(name="verifyLogin", fallback = "fallbackService")
	@RequestMapping("service")
	public ResponseEntity<String> verifyUser() throws SQLException, IOException {
		
		System.out.println(properties.getInstances());
		
		//String response = template.getForObject("http://localhost:8081/info", String.class);
		
		/*
		 * System.out.println("Count Value:--"+ count); count++; if(count>5 && count<11)
		 * { throw new RuntimeException("Its from Failure....."); }
		 */
		
		
		if(true) {
			throw new ResourceAccessException("");
		}
		
		
		//System.out.println("From response::--"+response);
		
		return ResponseEntity.ok().body("Success, There is no issue");
	}
	
	@TSBRetry(name = "backendA",fallback = "fallbackRetry")
	@RequestMapping("retry")
	public ResponseEntity<String> forRetryOp() {
		/*
		 * try { Thread.sleep(2000); } catch (InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		System.out.println("----forRetryOp---"+(count++));
		//String response = template.getForObject("http://localhost:8081/info", String.class);=
		if(true) {
		 throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "This is a remote exception");
		}
		return ResponseEntity.ok().body("Success, This from forRetryOp");
	}
	
	@TSBTimeLimiter(name = "backendA", fallback = "testTimlimiterFallback")
	@RequestMapping("testtimelimiter")
	public ResponseEntity<String> testTimelimiter(){
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.ok().body("Test timelimiter Success");
	}
	
	public ResponseEntity<String> testTimlimiterFallback(Throwable e){
		return ResponseEntity.ok().body("Test timlimiter fallback success");
	}
	
	@TSBTimeLimiter(name = "backendA", fallback = "timelimiterFallback")
	//@TSBCircuitBreaker(name="backendA", fallback = "timelimiterFallback")
	@RequestMapping("timelimiter")
	public CompletableFuture<String> forTimelimiterOp() {
		System.out.println("----forTimeLimiterOp---"+(count++));
		
		
		return CompletableFuture.supplyAsync(Resilience4jController::showTimelimiter);
	}
	
	public static String showTimelimiter() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Success, This from forTimelimiterOp";
	}
	
	public CompletableFuture<String> timelimiterFallback(Throwable e){
		System.out.println("timelimiterFallback calling..");
		return CompletableFuture.supplyAsync(()->"Success, This is from fallbackTimelimiter....");
	}
	
	public ResponseEntity<String> fallbackRetry(Throwable e){
		System.out.println("From fallback methods....");
		return ResponseEntity.ok().body("Success, This is from fallbackRetry....");
	}
	
	public ResponseEntity<String> fallbackService( Throwable e){
		System.out.println("From fallbackService:---"+e.getMessage());
		return ResponseEntity.ok().body("Success, issue is there, its from fallback::  "+prop.getConfigs());
	}

}
