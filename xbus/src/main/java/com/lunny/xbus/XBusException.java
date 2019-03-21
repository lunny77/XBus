package com.lunny.xbus;

public class XBusException extends RuntimeException {

    public XBusException(String message) {
        super(message);
    }

    public XBusException(String message, Throwable cause) {
        super(message, cause);
    }

    public XBusException(Throwable cause) {
        super(cause);
    }
}
