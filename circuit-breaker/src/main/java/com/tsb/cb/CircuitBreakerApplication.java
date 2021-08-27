package com.tsb.cb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import com.tsb.cb.config.AbstractCircuitBreakerConfigurationOnMissingBean;
import com.tsb.cb.config.CircuitBreakerAutoConfiguration;
import com.tsb.cb.config.CircuitBreakerConfigurationProperties;
import com.tsb.cb.config.TSBExceptionPredicate;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;

@SpringBootApplication
public class CircuitBreakerApplication {
	
	/*
	 * @Autowired ExceptionPredicate pred;
	 */
	
	/*
	 * @Autowired CircuitBreakerConfigurationProperties
	 * circuitBreakerConfigurationProperties;
	 */
	 

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
		
	}
	
	public static void main(String[] args) {
		SpringApplication.run(CircuitBreakerApplication.class, args);
		//new ExceptionPredicate().callShow();
	}
	
	
	/*
	 * @Bean
	 * 
	 * @Primary public CircuitBreakerAutoConfiguration getAutoConfig() {
	 * System.out.println("CircuitBreakerAutoConfiguration----"); return new
	 * CircuitBreakerAutoConfiguration(); }
	 * 
	 * public AbstractCircuitBreakerConfigurationOnMissingBean
	 * getAbstractConfigObj() {
	 * System.out.println(circuitBreakerConfigurationProperties.getInstances());
	 * return new AbstractCircuitBreakerConfigurationOnMissingBean(
	 * circuitBreakerConfigurationProperties); }
	 */
	 
	
	/*
	 * @Bean public RegistryEventConsumer<CircuitBreaker> myRegistryEventConsumer()
	 * {
	 * 
	 * return new RegistryEventConsumer<CircuitBreaker>() {
	 * 
	 * @Override public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker>
	 * entryAddedEvent) {
	 * entryAddedEvent.getAddedEntry().getEventPublisher().onEvent(event ->
	 * System.out.println(event.toString()));
	 * System.out.println("calling..onEntryAddedEvent"); }
	 * 
	 * @Override public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker>
	 * entryRemoveEvent) { System.out.println("calling..onEntryRemovedEvent"); }
	 * 
	 * @Override public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker>
	 * entryReplacedEvent) { System.out.println("calling..onEntryReplacedEvent"); }
	 * }; }
	 */

	/*
	 * @Bean public RegistryEventConsumer<Retry> myRetryRegistryEventConsumer() {
	 * 
	 * return new RegistryEventConsumer<Retry>() {
	 * 
	 * @Override public void onEntryAddedEvent(EntryAddedEvent<Retry>
	 * entryAddedEvent) {
	 * entryAddedEvent.getAddedEntry().getEventPublisher().onEvent(event ->
	 * LOG.info(event.toString())); }
	 * 
	 * @Override public void onEntryRemovedEvent(EntryRemovedEvent<Retry>
	 * entryRemoveEvent) {
	 * 
	 * }
	 * 
	 * @Override public void onEntryReplacedEvent(EntryReplacedEvent<Retry>
	 * entryReplacedEvent) {
	 * 
	 * } }; }
	 */
}
