package com.tsb.cb.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.vavr.CheckedFunction0;

public class FallbackDecorators {

	private List<FallbackDecorator> fallbackDecorators;
    private final FallbackDecorator defaultFallbackDecorator = new DefaultFallbackDecorator();

    public FallbackDecorators(List<FallbackDecorator> fallbackDecorators) {
        this.fallbackDecorators = fallbackDecorators;
    }
    

    /**
     * find a {@link FallbackDecorator} by return type of the {@link FallbackMethod} and decorate
     * supplier
     *
     * @param fallbackMethod fallback method that handles supplier's exception
     * @param supplier       original function
     * @return a function which is decorated by a {@link FallbackMethod}
     */
    public CheckedFunction0<Object> decorate(FallbackMethod fallbackMethod,
        CheckedFunction0<Object> supplier) {
        return get(fallbackMethod.getReturnType())
            .decorate(fallbackMethod, supplier);
    }

    private FallbackDecorator get(Class<?> returnType) {
        return fallbackDecorators.stream().filter(it -> it.supports(returnType))
            .findFirst()
            .orElse(defaultFallbackDecorator);
    }

    public List<FallbackDecorator> getFallbackDecorators() {
        return fallbackDecorators;
    }
}
