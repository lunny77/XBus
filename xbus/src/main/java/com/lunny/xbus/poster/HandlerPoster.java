package com.lunny.xbus.poster;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.lunny.xbus.Subscription;
import com.lunny.xbus.XBus;

public class HandlerPoster extends Handler implements Poster {
    private XBus xBus;

    public HandlerPoster(XBus xBus) {
        this.xBus = xBus;
    }

    @Override
    public void enqueue(Subscription subscription, Object event) {
        Message message = Message.obtain();
        message.obj = new PendingPost(event, subscription);
        sendMessage(message);
    }

    @Override
    public void handleMessage(Message msg) {
        PendingPost pendingPost = (PendingPost) msg.obj;
        xBus.invokeSubscriber(pendingPost.event, pendingPost.subscription);
    }
}
