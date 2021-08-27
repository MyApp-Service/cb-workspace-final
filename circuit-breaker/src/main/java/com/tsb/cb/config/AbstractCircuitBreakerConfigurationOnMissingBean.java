package com.tsb.cb.config;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.tsb.cb.aop.TSBCircuitBreakerAop;
import com.tsb.cb.events.DefaultEventConsumerRegistry;
import com.tsb.cb.events.EventConsumerRegistry;
import com.tsb.cb.service.CircuitBreakerAspectExt;
import com.tsb.cb.service.CompletionStageFallbackDecorator;
import com.tsb.cb.service.DefaultFallbackDecorator;
import com.tsb.cb.service.FallbackDecorator;
import com.tsb.cb.service.FallbackDecorators;
import com.tsb.cb.service.SpelResolver;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;

@Configuration
@Import({FallbackConfigurationOnMissingBean.class})
public class AbstractCircuitBreakerConfigurationOnMissingBean {

	@Autowired
	public  CircuitBreakerConfiguration circuitBreakerConfiguration;
	@Autowired
	public  CircuitBreakerConfigurationProperties circuitBreakerProperties;

	
	  public AbstractCircuitBreakerConfigurationOnMissingBean() { }
	 
	 public AbstractCircuitBreakerConfigurationOnMissingBean(
        CircuitBreakerConfigurationProperties circuitBreakerProperties) {
        this.circuitBreakerProperties = circuitBreakerProperties;
		
		 this.circuitBreakerConfiguration = new CircuitBreakerConfiguration(
		  circuitBreakerProperties);
		 
    }
    
    
	
	
	/*
	 * public CircuitBreakerConfiguration getConfig() {
	 * this.circuitBreakerConfiguration = new
	 * CircuitBreakerConfiguration(circuitBreakerProperties); return
	 * circuitBreakerConfiguration; }
	 */

    @Bean
    @ConditionalOnMissingBean(name = "compositeCircuitBreakerCustomizer")
    @Qualifier("compositeCircuitBreakerCustomizer")
    public CompositeCustomizer<CircuitBreakerConfigCustomizer> compositeCircuitBreakerCustomizer(
        List<CircuitBreakerConfigCustomizer> customizers) {
        return new CompositeCustomizer<>(customizers);
    }
    
    @Bean
    public CircuitBreakerConfigCustomizer testCustomizer() {
        return CircuitBreakerConfigCustomizer
            .of("backendA", builder -> builder.slidingWindowSize(100));
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry(
        EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry,
        RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer,
         CompositeCustomizer<CircuitBreakerConfigCustomizer> compositeCircuitBreakerCustomizer) {
        return circuitBreakerConfiguration
            .circuitBreakerRegistry(eventConsumerRegistry, circuitBreakerRegistryEventConsumer,
                compositeCircuitBreakerCustomizer);
    }

    @Bean
    @Primary
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer(
        Optional<List<RegistryEventConsumer<CircuitBreaker>>> optionalRegistryEventConsumers) {
        return circuitBreakerConfiguration
            .circuitBreakerRegistryEventConsumer(optionalRegistryEventConsumers);
    }

    @Bean
    @ConditionalOnMissingBean
    @Conditional(value = {AspectJOnClasspathCondition.class})
    public TSBCircuitBreakerAop circuitBreakerAspect(
        CircuitBreakerRegistry circuitBreakerRegistry,
			/* List<CircuitBreakerAspectExt> circuitBreakerAspectExtList, */
        FallbackDecorators fallbackDecorators
        //SpelResolver spelResolver
    ) {
        return circuitBreakerConfiguration
				.circuitBreakerAspect(
						circuitBreakerRegistry/* ,circuitBreakerAspectExtList */,
						fallbackDecorators/* ,spelResolver */);
    }
    
    /**
     * The EventConsumerRegistry is used to manage EventConsumer instances. The
     * EventConsumerRegistry is used by the CircuitBreakerHealthIndicator to show the latest
     * CircuitBreakerEvents events for each CircuitBreaker instance.
     *
     * @return a default EventConsumerRegistry {@link io.github.resilience4j.consumer.DefaultEventConsumerRegistry}
     */
    @Bean
    @Primary
    public EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry() {
        return new DefaultEventConsumerRegistry<>();
    }
    
    /*@Bean
    @Primary
    public FallbackDecorator fallbackDecorator( FallbackDecorator fallbackDecorator) {
    	System.out.println("calling fallbackDecorator!!!!!");
        return new DefaultFallbackDecorator();
    }*/
    @Bean
    @Primary
	public CompletionStageFallbackDecorator completionStageFallbackDecorator() {
    	System.out.println("calling fallbackDecorator!!!!! completionStageFallbackDecorator");
		return new CompletionStageFallbackDecorator();
	}

	/*
	 * @Bean
	 * 
	 * @Conditional(value = {RxJava2OnClasspathCondition.class,
	 * AspectJOnClasspathCondition.class})
	 * 
	 * @ConditionalOnMissingBean public RxJava2CircuitBreakerAspectExt
	 * rxJava2CircuitBreakerAspect() { return
	 * circuitBreakerConfiguration.rxJava2CircuitBreakerAspect(); }
	 */

	/*
	 * @Bean
	 * 
	 * @Conditional(value = {ReactorOnClasspathCondition.class,
	 * AspectJOnClasspathCondition.class})
	 * 
	 * @ConditionalOnMissingBean public ReactorCircuitBreakerAspectExt
	 * reactorCircuitBreakerAspect() { return
	 * circuitBreakerConfiguration.reactorCircuitBreakerAspect(); }
	 */
}
