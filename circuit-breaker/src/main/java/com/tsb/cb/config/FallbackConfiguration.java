package com.tsb.cb.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.tsb.cb.service.CompletionStageFallbackDecorator;
import com.tsb.cb.service.DefaultFallbackDecorator;
import com.tsb.cb.service.FallbackDecorator;
import com.tsb.cb.service.FallbackDecorators;

@Configuration
public class FallbackConfiguration {
	    @Bean
	    public FallbackDecorators fallbackDecorators( List<FallbackDecorator> fallbackDecorator) {
	        return new FallbackDecorators(fallbackDecorator);
	    }
	    }
