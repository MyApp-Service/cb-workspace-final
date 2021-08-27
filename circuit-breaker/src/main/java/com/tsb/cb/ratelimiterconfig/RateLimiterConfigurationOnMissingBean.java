package com.tsb.cb.ratelimiterconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tsb.cb.events.EventConsumerRegistry;

import io.github.resilience4j.ratelimiter.event.RateLimiterEvent;

@Configuration
public class RateLimiterConfigurationOnMissingBean extends
AbstractRateLimiterConfigurationOnMissingBean {

@Bean
@ConditionalOnMissingBean(value = RateLimiterEvent.class/* , parameterizedContainer = EventConsumerRegistry.class */)
public EventConsumerRegistry<RateLimiterEvent> rateLimiterEventsConsumerRegistry() {
    return rateLimiterConfiguration.rateLimiterEventsConsumerRegistry();
}

}
