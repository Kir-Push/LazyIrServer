package com.push.lazyir.utils.exceptions;

public class UdpRuntimeException extends RuntimeException {
    public UdpRuntimeException(String message) {
        super(message);
    }

    public UdpRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
