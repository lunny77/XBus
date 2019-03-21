package com.lunny.xbus.poster;

import com.lunny.xbus.Subscription;

public interface Poster {

    void enqueue(Subscription subscription, Object event);

}
