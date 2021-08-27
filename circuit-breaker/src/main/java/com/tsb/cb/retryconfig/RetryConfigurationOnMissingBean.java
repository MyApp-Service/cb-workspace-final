package com.tsb.cb.retryconfig;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.tsb.cb.config.CircuitBreakerConfiguration;
import com.tsb.cb.events.EventConsumerRegistry;
import com.tsb.cb.retrycore.ContextAwareScheduledThreadPoolExecutor;

import io.github.resilience4j.retry.event.RetryEvent;

@Configuration
public class RetryConfigurationOnMissingBean extends AbstractRetryConfigurationOnMissingBean {

    /**
     * The EventConsumerRegistry is used to manage EventConsumer instances. The
     * EventConsumerRegistry is used by the Retry events monitor to show the latest RetryEvent
     * events for each Retry instance.
     *
     * @return a default EventConsumerRegistry {@link DefaultEventConsumerRegistry}
     */
    @Bean
	@ConditionalOnMissingBean(value = RetryEvent.class/* , parameterizedContainer = EventConsumerRegistry.class */)
    public EventConsumerRegistry<RetryEvent> retryEventConsumerRegistry() {
        return retryConfiguration.retryEventConsumerRegistry();
    }
    
    @Bean
	  public RetryConfiguration getRetryConfiguration(){
		  return new RetryConfiguration();
	  }
    @Bean
    @Qualifier("contextAwareScheduledThreadPoolExecutor")
    public ContextAwareScheduledThreadPoolExecutor contextAwareScheduledThreadPoolExecutor() {
    	return new ContextAwareScheduledThreadPoolExecutor(0, null);
    }
}
