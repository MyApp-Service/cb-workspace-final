package com.tsb.cb.timelimiterconfig;

import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.tsb.cb.annotation.ConditionalOnAvailableEndpoint;
import com.tsb.cb.config.FallbackConfigurationOnMissingBean;
import com.tsb.cb.events.EventConsumerRegistry;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

@Configuration
@ConditionalOnClass(TimeLimiter.class)
@EnableConfigurationProperties(TimeLimiterProperties.class)
@Import({TimeLimiterConfigurationOnMissingBean.class, FallbackConfigurationOnMissingBean.class})
public class TimeLimiterAutoConfiguration {

	@Configuration
    @ConditionalOnClass(Endpoint.class)
    static class TimeLimiterAutoEndpointConfiguration {

        /*@Bean
        @ConditionalOnAvailableEndpoint
        public TimeLimiterEndpoint timeLimiterEndpoint(TimeLimiterRegistry timeLimiterRegistry) {
            return new TimeLimiterEndpoint(timeLimiterRegistry);
        }

        @Bean
        @ConditionalOnAvailableEndpoint
        public TimeLimiterEventsEndpoint timeLimiterEventsEndpoint(EventConsumerRegistry<TimeLimiterEvent> eventConsumerRegistry) {
            return new TimeLimiterEventsEndpoint(eventConsumerRegistry);
        }*/
    }
}
