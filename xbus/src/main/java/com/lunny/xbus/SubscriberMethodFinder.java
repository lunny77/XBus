package com.lunny.xbus;

import android.support.v4.util.ArrayMap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SubscriberMethodFinder {
    private final int MODIFIER_IGNORE = Modifier.STATIC | Modifier.ABSTRACT | Modifier.SYNCHRONIZED;
    private Map<Class<?>, List<SubscriberMethod>> cache;

    public SubscriberMethodFinder() {
        cache = new ArrayMap<>();
    }

    public List<SubscriberMethod> findSubscriberMethod(Class<?> subscriberClass) {
        List<SubscriberMethod> subscriberMethods = cache.get(subscriberClass);
        if (subscriberMethods != null && !subscriberMethods.isEmpty()) {
            return subscriberMethods;
        }
        return getMethodUsingReflection(subscriberClass);
    }

    private List<SubscriberMethod> getMethodUsingReflection(Class<?> subscriberCls) {
        List<SubscriberMethod> subscriberMethods = new ArrayList<>();
        Class<?> clazz = subscriberCls;
        //遍历subscriber及其的父类
        while (clazz != null && !skip(clazz)) {
            getMethodInSingleClass(subscriberMethods, clazz);
            clazz = clazz.getSuperclass();
        }
        return subscriberMethods;
    }

    private void getMethodInSingleClass(List<SubscriberMethod> subscriberMethods, Class<?> singleClass) {
        Method[] methods = singleClass.getDeclaredMethods();
        for (Method method : methods) {
            int modifier = method.getModifiers();
            //设置事件方法的规则：必须为public，不能为static、abstract、synchronized
            if ((modifier & Modifier.PUBLIC) != 0 && (modifier & MODIFIER_IGNORE) == 0) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes != null && parameterTypes.length == 1) {
                    Subscribe annotation = method.getAnnotation(Subscribe.class);
                    if (annotation != null) {
                        ThreadMode threadMode = annotation.threadMode();
                        int priority = annotation.priority();
                        boolean sticky = annotation.sticky();
                        subscriberMethods.add(new SubscriberMethod(method, parameterTypes[0], threadMode, priority, sticky));
                    }
                }
            }
        }
    }

    private boolean skip(Class<?> clazz) {
        String className = clazz.getSimpleName();
        if (className.startsWith("java.") || className.startsWith("javax.") || className.startsWith("android.")) {
            return true;
        }
        return false;
    }
}
