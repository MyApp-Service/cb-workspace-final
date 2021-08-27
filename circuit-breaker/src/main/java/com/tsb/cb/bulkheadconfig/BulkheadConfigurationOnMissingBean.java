package com.tsb.cb.bulkheadconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tsb.cb.events.EventConsumerRegistry;
import com.tsb.cb.retryconfig.RetryConfiguration;

import io.github.resilience4j.bulkhead.event.BulkheadEvent;

@Configuration
public class BulkheadConfigurationOnMissingBean extends AbstractBulkheadConfigurationOnMissingBean {

    /**
     * The EventConsumerRegistry is used to manage EventConsumer instances. The
     * EventConsumerRegistry is used by the BulkheadHealthIndicator to show the latest Bulkhead
     * events for each Bulkhead instance.
     *
     * @return a default EventConsumerRegistry {@link DefaultEventConsumerRegistry}
     */
    @Bean
	@ConditionalOnMissingBean(value = BulkheadEvent.class/* , parameterizedContainer = EventConsumerRegistry.class */)
    public EventConsumerRegistry<BulkheadEvent> bulkheadEventConsumerRegistry() {
        return bulkheadConfiguration.bulkheadEventConsumerRegistry();
    }
    
    @Bean
	  public BulkheadConfiguration getBulkheadConfiguration(){
		  return new BulkheadConfiguration();
	  }
    
    @Bean
	  public ThreadPoolBulkheadConfiguration getThreadPoolBulkheadConfiguration(){
		  return new ThreadPoolBulkheadConfiguration();
	  }
}
