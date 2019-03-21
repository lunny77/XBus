package com.lunny.xbus.poster;

import com.lunny.xbus.Subscription;
import com.lunny.xbus.XBus;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AsyncPoster implements Poster, Runnable {
    private XBus xBus;
    private BlockingQueue<PendingPost> queue;

    public AsyncPoster(XBus xBus) {
        this.xBus = xBus;
        this.queue = new ArrayBlockingQueue<>(10, true);
    }

    @Override
    public void enqueue(Subscription subscription, Object event) {
        queue.offer(new PendingPost(event, subscription));
        xBus.getExecutor().execute(this);
    }

    @Override
    public void run() {
        PendingPost pendingPost = queue.poll();
        xBus.invokeSubscriber(pendingPost.event, pendingPost.subscription);
    }
}
