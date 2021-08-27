package com.tsb.cb.service;

import java.lang.reflect.Method;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import com.tsb.cb.beans.SpelRootObject;
public class SpelResolver implements EmbeddedValueResolverAware {
    private static final String BEAN_SPEL_REGEX = "^[$#]\\{.+}$";
    private static final String METHOD_SPEL_REGEX = "^#.+$";

    private final SpelExpressionParser expressionParser;
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private StringValueResolver stringValueResolver;

    public SpelResolver(SpelExpressionParser spelExpressionParser, ParameterNameDiscoverer parameterNameDiscoverer) {
        this.expressionParser = spelExpressionParser;
        this.parameterNameDiscoverer = parameterNameDiscoverer;
    }

    public String resolve(Method method, Object[] arguments, String spelExpression) {
        if (StringUtils.isEmpty(spelExpression)) {
            return spelExpression;
        }

        if (spelExpression.matches(BEAN_SPEL_REGEX) && stringValueResolver != null) {
            return stringValueResolver.resolveStringValue(spelExpression);
        }

        if (spelExpression.matches(METHOD_SPEL_REGEX)) {
            SpelRootObject rootObject = new SpelRootObject(method, arguments);
            MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(rootObject, method, arguments, parameterNameDiscoverer);
            Object evaluated = expressionParser.parseExpression(spelExpression).getValue(evaluationContext);

            return (String) evaluated;
        }

        return spelExpression;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }
}
