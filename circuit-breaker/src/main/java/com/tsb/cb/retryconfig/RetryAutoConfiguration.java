package com.tsb.cb.retryconfig;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.tsb.cb.annotation.ConditionalOnAvailableEndpoint;
import com.tsb.cb.config.FallbackConfigurationOnMissingBean;
import com.tsb.cb.events.EventConsumerRegistry;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.event.RetryEvent;

@Configuration
@ConditionalOnClass(Retry.class)
@EnableConfigurationProperties(RetryProperties.class)
@Import({RetryConfigurationOnMissingBean.class, FallbackConfigurationOnMissingBean.class})
public class RetryAutoConfiguration {

	@Configuration
    @ConditionalOnClass(Endpoint.class)
    static class RetryAutoEndpointConfiguration {

        /*@Bean
        @ConditionalOnAvailableEndpoint
        public RetryEndpoint retryEndpoint(RetryRegistry retryRegistry) {
            return new RetryEndpoint(retryRegistry);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        public RetryEventsEndpoint retryEventsEndpoint(
            EventConsumerRegistry<RetryEvent> eventConsumerRegistry) {
            return new RetryEventsEndpoint(eventConsumerRegistry);
        }*/
    }
}
