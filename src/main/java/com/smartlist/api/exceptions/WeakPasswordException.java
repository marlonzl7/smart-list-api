package com.smartlist.api.exceptions;

public class WeakPasswordException extends CustomException {
    public WeakPasswordException(String code, String message) {
        super(code, message);
    }
}
