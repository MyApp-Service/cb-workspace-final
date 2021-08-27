package com.tsb.cb.retrycore;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamingThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String prefix;

    public NamingThreadFactory(String name) {
        this.group = getThreadGroup();
        this.prefix = String.join("-",name, "");
    }

    private ThreadGroup getThreadGroup() {
        SecurityManager security = System.getSecurityManager();
        return security != null ? security.getThreadGroup()
            : Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(group, runnable, createName(), 0);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }

    private String createName() {
        return prefix + threadNumber.getAndIncrement();
    }
}

