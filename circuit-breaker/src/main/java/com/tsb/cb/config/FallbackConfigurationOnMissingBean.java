package com.tsb.cb.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.tsb.cb.service.DefaultFallbackDecorator;
import com.tsb.cb.service.FallbackDecorator;
import com.tsb.cb.service.FallbackDecorators;

@Configuration
public class FallbackConfigurationOnMissingBean {

	 private final FallbackConfiguration fallbackConfiguration;
	 private final FallbackDecorator defaultFallbackDecorator = new DefaultFallbackDecorator();
	
	    public FallbackConfigurationOnMissingBean() {
	        this.fallbackConfiguration = new FallbackConfiguration();
	    }

	    @Bean
	    @ConditionalOnMissingBean
	    public FallbackDecorators fallbackDecorators( List<FallbackDecorator> fallbackDecorators) {
	        return fallbackConfiguration.fallbackDecorators(fallbackDecorators);
	    }
	    
	    
	   
	   
}
