package com.smartlist.api.exceptions;

public class InvalidCredentials extends CustomException {
    public InvalidCredentials(String code, String message) {
        super(code, message);
    }
}
