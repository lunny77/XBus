package com.lunny.xbus.poster;

import com.lunny.xbus.Subscription;

public class PendingPost {
    Object event;
    Subscription subscription;

    public PendingPost(Object event, Subscription subscription) {
        this.event = event;
        this.subscription = subscription;
    }
}
