package com.tsb.cb.bulkheadconfig;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.tsb.cb.annotation.ConditionalOnAvailableEndpoint;
import com.tsb.cb.config.FallbackConfigurationOnMissingBean;
import com.tsb.cb.events.EventConsumerRegistry;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.bulkhead.event.BulkheadEvent;

@Configuration
@ConditionalOnClass(Bulkhead.class)
@EnableConfigurationProperties({BulkheadProperties.class, ThreadPoolBulkheadProperties.class})
@Import({BulkheadConfigurationOnMissingBean.class, FallbackConfigurationOnMissingBean.class})
public class BulkheadAutoConfiguration {

	@Configuration
    @ConditionalOnClass(Endpoint.class)
    static class BulkheadEndpointAutoConfiguration {

        /*@Bean
        @ConditionalOnAvailableEndpoint
        public BulkheadEndpoint bulkheadEndpoint(BulkheadRegistry bulkheadRegistry,
            ThreadPoolBulkheadRegistry threadPoolBulkheadRegistry) {
            return new BulkheadEndpoint(bulkheadRegistry, threadPoolBulkheadRegistry);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        public BulkheadEventsEndpoint bulkheadEventsEndpoint(
            EventConsumerRegistry<BulkheadEvent> eventConsumerRegistry) {
            return new BulkheadEventsEndpoint(eventConsumerRegistry);
        }*/
    }
}
