package com.tsb.cb.service;


import org.springframework.stereotype.Component;

import com.tsb.cb.exception.IllegalReturnTypeException;

import io.vavr.CheckedFunction0;

@Component
public class DefaultFallbackDecorator implements FallbackDecorator {

    @Override
    public boolean supports(Class<?> target) {
        return true;
    }

    @Override
    public CheckedFunction0<Object> decorate(FallbackMethod fallbackMethod,
        CheckedFunction0<Object> supplier) {
        return () -> {
            try {
                return supplier.apply();
            } catch (IllegalReturnTypeException e) {
                throw e;
            } catch (Throwable throwable) {
                return fallbackMethod.fallback(throwable);
            }
        };
    }
}
