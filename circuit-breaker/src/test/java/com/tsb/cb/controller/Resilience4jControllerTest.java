package com.tsb.cb.controller;

import org.aspectj.apache.bcel.classfile.Method;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.tsb.cb.AppTests;
import com.tsb.cb.config.CircuitBreakerConfigurationProperties;
import com.tsb.cb.config.CircuitBreakerProperties;
import com.tsb.cb.service.CompletionStageFallbackDecorator;
import com.tsb.cb.service.FallbackMethod;


//@PrepareForTest({ApplicationContext.class})


public class Resilience4jControllerTest extends AppTests{
	
	 protected MockMvc mvc;
	 
	 @Autowired
	 CircuitBreakerProperties prop;
	 
	   @Autowired
	   WebApplicationContext webApplicationContext;

	   @Before
	   public void setUp() {
	      mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	   }
	
	

	//@Test
	public void sampleTest() throws Exception {
		
		/*
		 * RestTemplate restTemplate = new RestTemplate();
		 * 
		 * final String baseUrl = "http://localhost:8080/api/service";
		 * 
		 * URI uri = new URI(baseUrl);
		 * 
		 * String entity = restTemplate.getForObject(uri, String.class);
		 * 
		 * System.out.println(entity);
		 */
		/*
		 * MockHttpServletRequestBuilder builder = new RequestBuilder() {
		 * 
		 * @Override public MockHttpServletRequest buildRequest(ServletContext
		 * servletContext) { // TODO Auto-generated method stub return null; } };
		 */
		
		///new RestTemplate().getForObject("http://localhost:8080/api/service", String.class);
		
		
	     //mockMvc.perform()
	    //new Resilience4jController().verifyUser();
		
		System.out.println(prop.getInstances());
		
		String uri = "/api/retry";
		   MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
		      .accept(MediaType.APPLICATION_JSON)).andReturn();
		   
		   int status = mvcResult.getResponse().getStatus();
		   System.out.println(status);
			/*
			 * assertEquals(200, status); String content =
			 * mvcResult.getResponse().getContentAsString(); Product[] productlist =
			 * super.mapFromJson(content, Product[].class); assertTrue(productlist.length >
			 * 0);
			 */
		
		
	}
	
	//@Test
	public void sampleTest2() throws Exception {
		
		/*
		 * RestTemplate restTemplate = new RestTemplate();
		 * 
		 * final String baseUrl = "http://localhost:8080/api/service";
		 * 
		 * URI uri = new URI(baseUrl);
		 * 
		 * String entity = restTemplate.getForObject(uri, String.class);
		 * 
		 * System.out.println(entity);
		 */
		/*
		 * MockHttpServletRequestBuilder builder = new RequestBuilder() {
		 * 
		 * @Override public MockHttpServletRequest buildRequest(ServletContext
		 * servletContext) { // TODO Auto-generated method stub return null; } };
		 */
		
		///new RestTemplate().getForObject("http://localhost:8080/api/service", String.class);
		
		
	     //mockMvc.perform()
	    //new Resilience4jController().verifyUser();
		
		String uri = "/api/service";
		   MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
		      .accept(MediaType.APPLICATION_JSON)).andReturn();
		   
		   int status = mvcResult.getResponse().getStatus();
		   System.out.println(status);
			/*
			 * assertEquals(200, status); String content =
			 * mvcResult.getResponse().getContentAsString(); Product[] productlist =
			 * super.mapFromJson(content, Product[].class); assertTrue(productlist.length >
			 * 0);
			 */
		
		
	}
	
	//@Test
	public void sampleTest3() throws Exception {
		
		System.out.println(prop.getInstances());
		CircuitBreakerConfigurationProperties.InstanceProperties ins= new CircuitBreakerConfigurationProperties.InstanceProperties();
		System.out.println("ins.isEnableCircuitBreaker()::---"+ins.isEnableCircuitBreaker());
		System.out.println("ins.isEnableCircuitBreaker()::---"+ins.getBaseConfig());
		System.out.println("ins.isEnableCircuitBreaker()::---"+ins.getEventConsumerBufferSize());
		System.out.println("ins.isEnableCircuitBreaker()::---"+ins.getFailureRateThreshold());
		System.out.println("ins.isEnableCircuitBreaker()::---"+ins.getPermittedNumberOfCallsInHalfOpenState());
		System.out.println("ins.isEnableCircuitBreaker()::---"+ins.getRandomizedWaitFactor());
		System.out.println("ins.isEnableCircuitBreaker()::---"+ins.getSlowCallRateThreshold());
		mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		
		String uri = "/api/timelimiter";
		   MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
		      .accept(MediaType.APPLICATION_JSON)).andReturn();
		   
		   int status = mvcResult.getResponse().getStatus();
		   System.out.println(status);
			
		
		
	}
	
	//@Test
	public void sampleTest4() throws Exception {
		mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		
		String uri = "/api/testtimelimiter";
		   MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
		      .accept(MediaType.APPLICATION_JSON)).andReturn();
		   
		   int status = mvcResult.getResponse().getStatus();
		   System.out.println(status);
			
		
		
	}
	
	@Test
	public void sampleTest5() throws NoSuchMethodException {
		Class c = Resilience4jController.class;
		FallbackMethod fallbackMethod = FallbackMethod.create("fallbackService", c.getMethods()[0], c.getMethods()[0].getGenericParameterTypes(),new Resilience4jController());
		CompletionStageFallbackDecorator com = new CompletionStageFallbackDecorator();
		com.decorate(fallbackMethod, ()->{return "Hi";});
		com.supports(Resilience4jController.class);
		
	}
	
	@Test
	public void sampleTest6() throws NoSuchMethodException {
		Class c = Resilience4jController.class;
		FallbackMethod fallbackMethod = FallbackMethod.create("fallbackService", c.getMethods()[0], c.getMethods()[0].getGenericParameterTypes(),Resilience4jController.class);
		CompletionStageFallbackDecorator com = new CompletionStageFallbackDecorator();
		com.decorate(fallbackMethod, ()->{return "Hi";});
		com.supports(Resilience4jController.class);
		
	}
	
	public ResponseEntity<String> fallbackService(Throwable e){
		System.out.println("From fallbackService:---"+e.getMessage());
		return ResponseEntity.ok().body("Success, issue is there, its from fallback");
	}
}
