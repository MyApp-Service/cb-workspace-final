package com.tsb.cb.aop;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

//import com.bs.proteo.microservices.architecture.core.log.MicroserviceLogger;
//import com.bs.proteo.microservices.architecture.core.log.MicroserviceLoggerFactory;
import com.tsb.cb.annotation.AnnotationExtractor;
import com.tsb.cb.annotation.TSBCircuitBreaker;
import com.tsb.cb.config.CircuitBreakerConfigProperties;
import com.tsb.cb.config.CircuitBreakerConfigurationProperties;
import com.tsb.cb.config.CircuitBreakerConfigurationProperties.InstanceProperties;
import com.tsb.cb.config.TSBExceptionPredicate;
import com.tsb.cb.service.CircuitBreakerAspectExt;
import com.tsb.cb.service.FallbackDecorators;
import com.tsb.cb.service.FallbackMethod;
import com.tsb.cb.service.SpelResolver;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.lang.Nullable;

@Aspect
public class TSBCircuitBreakerAop implements Ordered {
	
	private static final Logger logger = LoggerFactory.getLogger(TSBCircuitBreakerAop.class);
	//private static MicroserviceLogger logger= MicroserviceLoggerFactory.getLogger(TSBCircuitBreakerAop.class);;
	@Autowired
	private Environment env;
	
	private final CircuitBreakerConfigurationProperties circuitBreakerProperties;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    //private final @Nullable
    //List<CircuitBreakerAspectExt> circuitBreakerAspectExtList;
    private final FallbackDecorators fallbackDecorators;
    //private final SpelResolver spelResolver;
    
    @Autowired
    TSBExceptionPredicate predicate;

	public TSBCircuitBreakerAop(CircuitBreakerConfigurationProperties circuitBreakerProperties,
                                CircuitBreakerRegistry circuitBreakerRegistry,
			/* List<CircuitBreakerAspectExt> circuitBreakerAspectExtList, */
                                FallbackDecorators fallbackDecorators
                                /*SpelResolver spelResolver*/) {
        this.circuitBreakerProperties = circuitBreakerProperties;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        //this.circuitBreakerAspectExtList = circuitBreakerAspectExtList;
        this.fallbackDecorators = fallbackDecorators;
        //this.spelResolver = spelResolver;
    }
	
	/*
	 * @Autowired private CircuitBreakerRestryService circuitBreakerRegistry;
	 */
	
	/*
	 * public TSBCircuitBreakerAop(FallbackDecorators decorators) { // TODO
	 * Auto-generated constructor stub fallbackdec }
	 */
	
	@Around(value = " @annotation(com.tsb.cb.annotation.TSBCircuitBreaker)")
    public Object circuitBreakerAroundAdvice(ProceedingJoinPoint proceedingJoinPoint
    		) throws Throwable {
	 
		logger.info("circuitBreakerAroundAdvice---");
		
		//String isEnabled = env.getProperty("resilience4j.circuitbreaker.enable");
		// param name changes to tsb.circuitbreaker.enable
		String isEnabled = "false";
		isEnabled =	env.getProperty("tsb.circuitbreaker.enable");
		System.out.println(" tsb.circuitbreaker.enable "+isEnabled);
		if("false".equalsIgnoreCase(isEnabled)) {
			
			return proceedingJoinPoint.proceed();
		}
		
        Method method = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod();
        String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
        
        logger.info("Method Name:--"+methodName);
        
        //Class cls = proceedingJoinPoint.getTarget().getClass();
		
		//Method m = cls.getMethod(proceedingJoinPoint.getSignature().getName());
		
		TSBCircuitBreaker circuitBreakerAnnotation = method.getAnnotation(TSBCircuitBreaker.class);
		
        
        if (circuitBreakerAnnotation == null) {
            circuitBreakerAnnotation = getCircuitBreakerAnnotation(proceedingJoinPoint);
        }
        if (circuitBreakerAnnotation == null) { //because annotations wasn't found
            return proceedingJoinPoint.proceed();
        }
        //String backend = spelResolver.resolve(method, proceedingJoinPoint.getArgs(), circuitBreakerAnnotation.name());
       CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(
            methodName, circuitBreakerAnnotation.name());
        Class<?> returnType = method.getReturnType();
        
        System.out.println("-- Name: --"+circuitBreaker.getName());
        System.out.println("-- getMinimumNumberOfCalls: --"+circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls());
        System.out.println("-- getFailureRateThreshold: --"+circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold());
        System.out.println("-- getSlidingWindowSize: --"+circuitBreaker.getCircuitBreakerConfig().getSlidingWindowSize());
        System.out.println("-- getSlowCallRateThreshold: --"+circuitBreaker.getCircuitBreakerConfig().getSlowCallRateThreshold());
        System.out.println("-- getSlowCallDurationThreshold: --"+circuitBreaker.getCircuitBreakerConfig().getSlowCallDurationThreshold().getSeconds()+" seconds");
        System.out.println("-- getMaxWaitDurationInHalfOpenState: --"+circuitBreaker.getCircuitBreakerConfig().getMaxWaitDurationInHalfOpenState().getSeconds()+" seconds");
        
        /*InstanceProperties instance = circuitBreakerProperties.findCircuitBreakerProperties(circuitBreaker.getName()).get();
        boolean flag = instance.isEnableCircuitBreaker();
        System.out.println(circuitBreaker.getName()+" is enabled : "+flag);
        // need to check the global flag as well
        if(!flag) {
        	return proceedingJoinPoint.proceed();
        }*/

		
		/*
		 * String fallbackMethodValue = spelResolver.resolve(method,
		 * proceedingJoinPoint.getArgs(), circuitBreakerAnnotation.fallback());
		 */
		if (StringUtils.isEmpty(circuitBreakerAnnotation.fallback())) {
			return proceed(proceedingJoinPoint, methodName, circuitBreaker, returnType);
		}
		predicate.setExceptions(circuitBreakerProperties.getPredicateExceptions());
        System.out.println("circuitBreaker.getName():----"+circuitBreaker.getName());
        FallbackMethod fallbackMethod = FallbackMethod
            .create(circuitBreakerAnnotation.fallback(), method,
                proceedingJoinPoint.getArgs(), proceedingJoinPoint.getTarget());
        return fallbackDecorators.decorate(fallbackMethod,
            () -> proceed(proceedingJoinPoint, methodName, circuitBreaker, returnType)).apply();
    }
	
	private Object proceed(ProceedingJoinPoint proceedingJoinPoint, String methodName,
	        CircuitBreaker circuitBreaker, Class<?> returnType)
	        throws Throwable {
	        /*if (circuitBreakerAspectExtList != null && !circuitBreakerAspectExtList.isEmpty()) {
	            for (CircuitBreakerAspectExt circuitBreakerAspectExt : circuitBreakerAspectExtList) {
	                if (circuitBreakerAspectExt.canHandleReturnType(returnType)) {
	                    return circuitBreakerAspectExt
	                        .handle(proceedingJoinPoint, circuitBreaker, methodName);
	                }
	            }
	        }*/
		System.out.println("CompletionStage.class.isAssignableFrom(returnType) from CB:_----"+CompletionStage.class.isAssignableFrom(returnType)+" returnTYpe: "+ returnType);
	        if (CompletionStage.class.isAssignableFrom(returnType)) {
	            return handleJoinPointCompletableFuture(proceedingJoinPoint, circuitBreaker);
	        }
	        return defaultHandling(proceedingJoinPoint, circuitBreaker);
	    }

	    private CircuitBreaker getOrCreateCircuitBreaker(
	        String methodName, String backend) {
			
			
			  CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(backend);
			 
			 
	    	
			/*
			 * CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
			 * .failureRateThreshold(50) .waitDurationInOpenState(Duration.ofMillis(1000))
			 * .permittedNumberOfCallsInHalfOpenState(10) .slidingWindowSize(2)
			 * .recordExceptions(IOException.class, TimeoutException.class) .build();
			 */

	    		// Create a CircuitBreakerRegistry with a custom global configuration
	    		//CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(circuitBreakerConfig);
	    		
	    		//CircuitBreaker circuitBreaker = registry.circuitBreaker(backend);

			/*
			 * if (logger.isDebugEnabled()) { logger.debug(
			 * "Created or retrieved circuit breaker '{}' with failure rate '{}' for method: '{}'"
			 * , backend,
			 * circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold(),
			 * methodName); }
			 */

	        return circuitBreaker;
	    }

	    @Nullable
	    private TSBCircuitBreaker getCircuitBreakerAnnotation(ProceedingJoinPoint proceedingJoinPoint) {
			
	    	
	        if (proceedingJoinPoint.getTarget() instanceof Proxy) {
	            
	            return AnnotationExtractor
	                .extractAnnotationFromProxy(proceedingJoinPoint.getTarget(), TSBCircuitBreaker.class);
	        } else {
	            return AnnotationExtractor
	                .extract(proceedingJoinPoint.getTarget().getClass(), TSBCircuitBreaker.class);
	        }
	    	
	    }
	    
	    /**
	     * handle the CompletionStage return types AOP based into configured circuit-breaker
	     */
	    private Object handleJoinPointCompletableFuture(ProceedingJoinPoint proceedingJoinPoint,
	        CircuitBreaker circuitBreaker) {
	        return circuitBreaker.executeCompletionStage(() -> {
	            try {
	                return (CompletionStage<?>) proceedingJoinPoint.proceed();
	            } catch (Throwable throwable) {
	                throw new CompletionException(throwable);
	            }
	        });
	    }

	    /**
	     * the default Java types handling for the circuit breaker AOP
	     */
	    private Object defaultHandling(ProceedingJoinPoint proceedingJoinPoint,
	        CircuitBreaker circuitBreaker) throws Throwable {
	        return circuitBreaker.executeCheckedSupplier(proceedingJoinPoint::proceed);
	    }

		@Override
		public int getOrder() {
			// TODO Auto-generated method stub
			return 0;//circuitBreakerProperties.getCircuitBreakerAspectOrder();
		}

}
