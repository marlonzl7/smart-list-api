package com.smartlist.api.exceptions;

public class EmailAlreadyExistsException extends CustomException {
    public EmailAlreadyExistsException(String code, String message) {
        super(code, message);
    }
}
