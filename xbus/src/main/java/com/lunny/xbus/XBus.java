package com.lunny.xbus;

import android.os.Looper;
import android.support.v4.util.ArrayMap;

import com.lunny.xbus.poster.AsyncPoster;
import com.lunny.xbus.poster.HandlerPoster;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class XBus {
    private volatile static XBus INSTANCE;
    private final SubscriberMethodFinder subscriberMethodFinder;
    private final Map<Class<?>, List<Subscription>> subscriptionByEventType;
    private final Map<Object, List<Class<?>>> typesBySubscriber;

    private ExecutorService executor;
    private HandlerPoster handlerPoster;
    private AsyncPoster asyncPoster;

    private XBus() {
        subscriberMethodFinder = new SubscriberMethodFinder();
        subscriptionByEventType = new ArrayMap<>();
        typesBySubscriber = new ArrayMap<>();
        executor = Executors.newFixedThreadPool(4, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(false);
                thread.setName("xbus-thread-pool");
                return thread;
            }
        });
        handlerPoster = new HandlerPoster(this);
        asyncPoster = new AsyncPoster(this);
    }

    public static XBus getInstance() {
        if (INSTANCE == null) {
            synchronized (XBus.class) {
                if (INSTANCE == null) {
                    INSTANCE = new XBus();
                }
            }
        }
        return INSTANCE;
    }

    public void register(Object subscriber) {
        Class<?> clazz = subscriber.getClass();
        List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethod(clazz);
        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            subscribe(subscriber, subscriberMethod);
        }
    }

    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        Class<?> eventType = subscriberMethod.getEventType();
        Subscription subscription = new Subscription(subscriber, subscriberMethod);
        List<Subscription> subscriptionList = subscriptionByEventType.get(eventType);
        if (subscriptionList == null) {
            subscriptionList = new ArrayList<>();
            subscriptionByEventType.put(eventType, subscriptionList);
        } else {
            if (subscriptionList.contains(subscription)) {
                throw new XBusException("Subscription " + subscription + " already register to event:" + eventType);
            }
        }

        int size = subscriptionList.size();
        for (int i = 0; i <= size; i++) {
            if (i == size || subscriberMethod.priority > subscriptionList.get(i).getSubscriberMethod().priority) {
                subscriptionList.add(i, subscription);
                break;
            }
        }

        List<Class<?>> eventOfSubscriber = typesBySubscriber.get(subscriber);
        if (eventOfSubscriber == null) {
            eventOfSubscriber = new ArrayList<>();
            typesBySubscriber.put(subscriber, eventOfSubscriber);
        }
        eventOfSubscriber.add(subscriberMethod.eventType);
    }

    public void unregister(Object subscriber) {
        List<Class<?>> eventTypeList = typesBySubscriber.get(subscriber);
        if (eventTypeList == null)
            throw new XBusException("when unregister subscriber, make sure register first!");
        for (Class eventType : eventTypeList) {
            unregisterByEventType(subscriber, eventType);
        }
        typesBySubscriber.remove(subscriber);
    }

    private void unregisterByEventType(Object subscriber, Class<?> eventType) {
        List<Subscription> subscriptionList = subscriptionByEventType.get(eventType);
        if (subscriptionList != null) {
            for (Subscription subscription : subscriptionList) {
                if (subscription.getSubscriber() == subscriber) {
                    subscriptionList.remove(subscription);
                }
            }
        }
    }

    public void post(Object event) {
        Class<?> eventType = event.getClass();
        List<Subscription> subscriptionList = subscriptionByEventType.get(eventType);
        for (Subscription subscription : subscriptionList) {
            postToSubscription(event, subscription);
        }
    }

    private void postToSubscription(Object event, Subscription subscription) {
        SubscriberMethod subscriberMethod = subscription.getSubscriberMethod();
        switch (subscriberMethod.threadMode) {
            case POSTING:
                invokeSubscriber(event, subscription);
                break;

            case MAIN:
                if (isMainThread()) {
                    invokeSubscriber(event, subscription);
                } else {
                    handlerPoster.enqueue(subscription, event);
                }
                break;

            case MAIN_ORDERED:
                //todo
                if (isMainThread()) {
                    invokeSubscriber(event, subscription);
                } else {
                    handlerPoster.enqueue(subscription, event);
                }
                break;

            case BACKGROUND:
                if (isMainThread()) {
                    asyncPoster.enqueue(subscription, event);
                } else {
                    invokeSubscriber(event, subscription);
                }
                break;

            case ASYNC:
                asyncPoster.enqueue(subscription, event);
                break;
        }

    }

    public void invokeSubscriber(Object event, Subscription subscription) {
        Object subscriber = subscription.getSubscriber();
        SubscriberMethod subscriberMethod = subscription.getSubscriberMethod();
        try {
            subscriberMethod.method.invoke(subscriber, event);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
