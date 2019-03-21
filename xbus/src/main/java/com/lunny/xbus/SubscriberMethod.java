package com.lunny.xbus;

import java.lang.reflect.Method;

public class SubscriberMethod {
    Method method;
    Class<?> eventType;
    ThreadMode threadMode;
    int priority;
    boolean sticky;

    public SubscriberMethod(Method method, Class<?> eventType, ThreadMode threadMode, int priority, boolean sticky) {
        this.method = method;
        this.eventType = eventType;
        this.threadMode = threadMode;
        this.priority = priority;
        this.sticky = sticky;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getEventType() {
        return eventType;
    }

    public ThreadMode getThreadMode() {
        return threadMode;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isSticky() {
        return sticky;
    }
}
