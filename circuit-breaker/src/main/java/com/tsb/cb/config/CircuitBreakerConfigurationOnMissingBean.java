package com.tsb.cb.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tsb.cb.events.EventConsumerRegistry;

import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;

@Configuration
public class CircuitBreakerConfigurationOnMissingBean extends
AbstractCircuitBreakerConfigurationOnMissingBean {
	
	@Autowired
	public CircuitBreakerConfigurationOnMissingBean(CircuitBreakerConfigurationProperties circuitBreakerProperties) {
		super(circuitBreakerProperties);
		//this.circuitBreakerProperties = circuitBreakerProperties;
	}
	 
	
	  public CircuitBreakerConfigurationOnMissingBean() { }
	  
	  @Bean
	  public CircuitBreakerConfiguration getCircuitBreakerConfiguration(){
		  return new CircuitBreakerConfiguration(this.circuitBreakerProperties);
	  }
	 

//@Bean
//@ConditionalOnMissingBean(value = CircuitBreakerEvent.class, parameterizedContainer = EventConsumerRegistry.class)
//public EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry() {
    //return circuitBreakerConfiguration.eventConsumerRegistry();
//}

}
