package com.tsb.cb.timelimiterconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tsb.cb.events.EventConsumerRegistry;

import io.github.resilience4j.timelimiter.event.TimeLimiterEvent;

@Configuration
public class TimeLimiterConfigurationOnMissingBean extends AbstractTimeLimiterConfigurationOnMissingBean {

    /**
     * The EventConsumerRegistry is used to manage EventConsumer instances.
     * The EventConsumerRegistry is used by the TimeLimiter events monitor to show the latest TimeLimiterEvent events
     * for each TimeLimiter instance.
     *
     * @return a default EventConsumerRegistry {@link DefaultEventConsumerRegistry}
     */
    @Bean
	@ConditionalOnMissingBean(value = TimeLimiterEvent.class/* , parameterizedContainer = EventConsumerRegistry.class */)
    public EventConsumerRegistry<TimeLimiterEvent> timeLimiterEventsConsumerRegistry() {
        return timeLimiterConfiguration.timeLimiterEventsConsumerRegistry();
    }

}
