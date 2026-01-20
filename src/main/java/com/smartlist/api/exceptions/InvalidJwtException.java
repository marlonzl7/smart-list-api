package com.smartlist.api.exceptions;

public class InvalidJwtException extends CustomException {
    public InvalidJwtException(String code, String message) {
        super(code, message);
    }
}
