package com.tsb.cb.config;

import javax.xml.ws.Endpoint;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.tsb.cb.endpoint.CircuitBreakerEndpoint;
import com.tsb.cb.events.EventConsumerRegistry;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;

@Configuration
@ConditionalOnClass(CircuitBreaker.class)
@EnableConfigurationProperties(CircuitBreakerProperties.class)
@Import({ CircuitBreakerConfigurationOnMissingBean.class, FallbackConfigurationOnMissingBean.class })
public class CircuitBreakerAutoConfiguration {

	
	@Configuration

	@ConditionalOnClass(Endpoint.class)
	static class CircuitBreakerEndpointAutoConfiguration {

		@Bean

		//@ConditionalOnAvailableEndpoint
		public CircuitBreakerEndpoint circuitBreakerEndpoint(CircuitBreakerRegistry circuitBreakerRegistry) {
			return new CircuitBreakerEndpoint(circuitBreakerRegistry);
		}

		/*
		 * @Bean //@ConditionalOnAvailableEndpoint public CircuitBreakerEventsEndpoint
		 * circuitBreakerEventsEndpoint( EventConsumerRegistry<CircuitBreakerEvent>
		 * eventConsumerRegistry) { return new
		 * CircuitBreakerEventsEndpoint(eventConsumerRegistry); }
		 */

	}
	 
}
