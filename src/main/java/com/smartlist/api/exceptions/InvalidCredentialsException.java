package com.smartlist.api.exceptions;

public class InvalidCredentialsException extends CustomException {
    public InvalidCredentialsException(String code, String message) {
        super(code, message);
    }
}
