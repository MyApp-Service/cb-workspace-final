package com.tsb.cb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

//import com.bs.proteo.microservices.architecture.core.log.MicroserviceLogger;
//import com.bs.proteo.microservices.architecture.core.log.MicroserviceLoggerFactory;
import com.tsb.cb.annotation.AnnotationExtractor;

public class AspectJOnClasspathCondition implements Condition {
	
	private static Logger logger = LoggerFactory.getLogger(AspectJOnClasspathCondition.class);
	//private static MicroserviceLogger logger= MicroserviceLoggerFactory.getLogger(AspectJOnClasspathCondition.class);;
    private static final String CLASS_TO_CHECK = "org.aspectj.lang.ProceedingJoinPoint";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return AspectUtil.checkClassIfFound(context, CLASS_TO_CHECK, (e) -> logger
            .debug("Aspects are not activated because AspectJ is not on the classpath."));
    }

}
