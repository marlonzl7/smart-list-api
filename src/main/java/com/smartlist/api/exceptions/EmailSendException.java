package com.smartlist.api.exceptions;

public class EmailSendException extends CustomException {
    public EmailSendException(String code, String message) {
        super(code, message);
    }
}
