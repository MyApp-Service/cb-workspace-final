package com.tsb.cb.ratelimiterconfig;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.tsb.cb.config.FallbackConfigurationOnMissingBean;

import io.github.resilience4j.ratelimiter.RateLimiter;

@Configuration
@ConditionalOnClass(RateLimiter.class)
@EnableConfigurationProperties(RateLimiterProperties.class)
@Import({RateLimiterConfigurationOnMissingBean.class, FallbackConfigurationOnMissingBean.class})
public class RateLimiterAutoConfiguration {

	 @Configuration
	    @ConditionalOnClass( Endpoint.class)
	    static class RateLimiterEndpointAutoConfiguration {

	        /*@Bean
	        @ConditionalOnAvailableEndpoint
	        public RateLimiterEndpoint rateLimiterEndpoint(RateLimiterRegistry rateLimiterRegistry) {
	            return new RateLimiterEndpoint(rateLimiterRegistry);
	        }

	        @Bean
	        @ConditionalOnAvailableEndpoint
	        public RateLimiterEventsEndpoint rateLimiterEventsEndpoint(
	            EventConsumerRegistry<RateLimiterEvent> eventConsumerRegistry) {
	            return new RateLimiterEventsEndpoint(eventConsumerRegistry);
	        }*/
	    }
}
