package com.coditory.quark.uri;

public final class InvalidHttpUrlException extends RuntimeException {
    InvalidHttpUrlException(String message) {
        super(message);
    }

    InvalidHttpUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
