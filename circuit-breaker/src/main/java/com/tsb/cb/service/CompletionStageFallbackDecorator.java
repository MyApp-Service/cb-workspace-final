package com.tsb.cb.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import io.vavr.CheckedFunction0;

public class CompletionStageFallbackDecorator implements FallbackDecorator {

    @Override
    public boolean supports(Class<?> target) {
        return CompletionStage.class.isAssignableFrom(target);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CheckedFunction0<Object> decorate(FallbackMethod fallbackMethod,
        CheckedFunction0<Object> supplier) {
        return supplier.andThen(request -> {
            CompletionStage<Object> completionStage = (CompletionStage) request;
            CompletableFuture promise = new CompletableFuture();
            completionStage.whenComplete((result, throwable) -> {
                if (throwable != null){
                    if (throwable instanceof CompletionException || throwable instanceof ExecutionException) {
                        tryRecover(fallbackMethod, promise, throwable.getCause());
                    }else{
                        tryRecover(fallbackMethod, promise, throwable);
                    }
                } else {
                    promise.complete(result);
                }
            });

            return promise;
        });
    }

    @SuppressWarnings("unchecked")
    private void tryRecover(FallbackMethod fallbackMethod, CompletableFuture promise,
        Throwable throwable) {
        try {
            CompletionStage<Object> completionStage = (CompletionStage) fallbackMethod.fallback(throwable);
            completionStage.whenComplete((fallbackResult, fallbackThrowable) -> {
                    if (fallbackThrowable != null) {
                        promise.completeExceptionally(fallbackThrowable);
                    } else {
                        promise.complete(fallbackResult);
                    }
                });
        } catch (Throwable fallbackThrowable) {
            promise.completeExceptionally(fallbackThrowable);
        }
    }
}