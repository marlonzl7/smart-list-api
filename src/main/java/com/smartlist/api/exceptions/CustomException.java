package com.smartlist.api.exceptions;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final String code;
    private final String message;

    public CustomException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
