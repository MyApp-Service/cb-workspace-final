package com.tsb.cb.beans;

import java.lang.reflect.Method;

public class SpelRootObject {

	private final String methodName;
    private final Object[] args;

    public SpelRootObject(Method method, Object[] args) {
        this.methodName = method.getName();
        this.args = args;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }
}
