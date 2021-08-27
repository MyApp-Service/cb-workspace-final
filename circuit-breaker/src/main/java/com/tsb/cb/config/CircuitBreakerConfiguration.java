package com.tsb.cb.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

//import com.bs.proteo.microservices.architecture.core.log.MicroserviceLogger;
//import com.bs.proteo.microservices.architecture.core.log.MicroserviceLoggerFactory;
import com.tsb.cb.annotation.AnnotationExtractor;
import com.tsb.cb.aop.TSBCircuitBreakerAop;
import com.tsb.cb.config.CircuitBreakerConfigurationProperties.InstanceProperties;
import com.tsb.cb.events.CircularEventConsumer;
import com.tsb.cb.events.DefaultEventConsumerRegistry;
import com.tsb.cb.events.EventConsumerRegistry;
import com.tsb.cb.service.CircuitBreakerAspectExt;
import com.tsb.cb.service.FallbackDecorators;
import com.tsb.cb.service.SpelResolver;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import io.github.resilience4j.core.registry.CompositeRegistryEventConsumer;
import io.github.resilience4j.core.registry.RegistryEventConsumer;

@Configuration
public class CircuitBreakerConfiguration {
	
	@Autowired
	protected  CircuitBreakerConfigurationProperties circuitBreakerConfigurationProperties;
	//private static MicroserviceLogger logger= MicroserviceLoggerFactory.getLogger(AnnotationExtractor.class);;
	
	
	public CircuitBreakerConfiguration(CircuitBreakerConfigurationProperties circuitBreakerConfigurationProperties) {
		this.circuitBreakerConfigurationProperties = circuitBreakerConfigurationProperties;
		//logger.info("Hello from CircuitBreakerConfiguration");
		System.out.println("Hello from CircuitBreakerConfiguration");
	}
	 
	 
	 public CircuitBreakerConfiguration() { // TODO Auto-generated constructor
	 }
	 
	
	 @Bean
	    public CircuitBreakerRegistry circuitBreakerRegistry(
	        EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry,
	        RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer,
	        CompositeCustomizer<CircuitBreakerConfigCustomizer> compositeCircuitBreakerCustomizer) {

	        CircuitBreakerRegistry circuitBreakerRegistry = createCircuitBreakerRegistry(
	            circuitBreakerConfigurationProperties, circuitBreakerRegistryEventConsumer,
	            compositeCircuitBreakerCustomizer);
	        registerEventConsumer(circuitBreakerRegistry, eventConsumerRegistry);
	        // then pass the map here
	        initCircuitBreakerRegistry(circuitBreakerRegistry, compositeCircuitBreakerCustomizer);
	        return circuitBreakerRegistry;
	    }

	@Bean
	@Conditional( value = {AspectJOnClasspathCondition.class} )
	public TSBCircuitBreakerAop circuitBreakerAspect(CircuitBreakerRegistry registry,
			/* List<CircuitBreakerAspectExt> circuitBreakerAspectExtList, */
			FallbackDecorators fallbackDecorators
			/*SpelResolver spelResolver*/) {
		return new TSBCircuitBreakerAop(circuitBreakerConfigurationProperties,
				registry, /* circuitBreakerAspectExtList, */
				fallbackDecorators/* ,spelResolver */);
	}
	
	 /**
     * Registers the post creation consumer function that registers the consumer events to the
     * circuit breakers.
     *
     * @param circuitBreakerRegistry The circuit breaker registry.
     * @param eventConsumerRegistry  The event consumer registry.
     */
    public void registerEventConsumer(CircuitBreakerRegistry circuitBreakerRegistry,
                                      EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry) {
        circuitBreakerRegistry.getEventPublisher()
            .onEntryAdded(event -> registerEventConsumer(eventConsumerRegistry, event.getAddedEntry()))
            .onEntryReplaced(event -> registerEventConsumer(eventConsumerRegistry, event.getNewEntry()));
    }
    
    private void registerEventConsumer(
            EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry,
            CircuitBreaker circuitBreaker) {
    	//logger.info("circuitBreaker.getName() from registerEventConsumer:---"+circuitBreaker.getName());
            int eventConsumerBufferSize = 100;/*circuitBreakerConfigurationProperties
                .findCircuitBreakerProperties(circuitBreaker.getName())
                .map(InstanceProperties::getEventConsumerBufferSize)
                .orElse(100);*/
           CircularEventConsumer<CircuitBreakerEvent> cc=eventConsumerRegistry
            .createEventConsumer(circuitBreaker.getName(), eventConsumerBufferSize);
            circuitBreaker.getEventPublisher().onEvent(cc);
        }
    
    /**
     * Initializes the CircuitBreaker registry.
     *
     * @param circuitBreakerRegistry The circuit breaker registry.
     * @param customizerMap
     */
    void initCircuitBreakerRegistry(CircuitBreakerRegistry circuitBreakerRegistry,
        CompositeCustomizer<CircuitBreakerConfigCustomizer> customizerMap) {
        circuitBreakerConfigurationProperties.getInstances().forEach(
            (name, properties) -> circuitBreakerRegistry.circuitBreaker(name,
            		circuitBreakerConfigurationProperties
                    .createCircuitBreakerConfig(name, properties, customizerMap))
        );
    }
    
    /**
     * Initializes a circuitBreaker registry.
     *
     * @param circuitBreakerProperties The circuit breaker configuration properties.
     * @param customizerMap
     * @return a CircuitBreakerRegistry
     */
    CircuitBreakerRegistry createCircuitBreakerRegistry(
        CircuitBreakerConfigurationProperties circuitBreakerProperties,
        RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer,
        CompositeCustomizer<CircuitBreakerConfigCustomizer> customizerMap) {

    	System.out.println("ConfigsObj:----"+circuitBreakerProperties.getConfigs());
        Map<String, CircuitBreakerConfig> configs = circuitBreakerProperties.getConfigs()
            .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> circuitBreakerProperties
                    .createCircuitBreakerConfig(entry.getKey(), entry.getValue(),
                        customizerMap)));
        
        

        return CircuitBreakerRegistry.of(configs, circuitBreakerRegistryEventConsumer,
            io.vavr.collection.HashMap.ofAll(circuitBreakerProperties.getTags()));
    }
    
    @Bean
    @Primary
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerRegistryEventConsumer(
        Optional<List<RegistryEventConsumer<CircuitBreaker>>> optionalRegistryEventConsumers) {
        return new CompositeRegistryEventConsumer<>(
            optionalRegistryEventConsumers.orElseGet(ArrayList::new));
    }
	
    
    /**
     * The EventConsumerRegistry is used to manage EventConsumer instances. The
     * EventConsumerRegistry is used by the CircuitBreakerHealthIndicator to show the latest
     * CircuitBreakerEvents events for each CircuitBreaker instance.
     *
     * @return a default EventConsumerRegistry {@link io.github.resilience4j.consumer.DefaultEventConsumerRegistry}
     */
    @Bean
    public EventConsumerRegistry<CircuitBreakerEvent> eventConsumerRegistry() {
        return new DefaultEventConsumerRegistry<>();
    }
    
    
    
    
	
}
