package com.smartlist.api.exceptions;

public class PhoneNumberRequiredException extends CustomException {
    public PhoneNumberRequiredException(String code, String message) {
        super(code, message);
    }
}
