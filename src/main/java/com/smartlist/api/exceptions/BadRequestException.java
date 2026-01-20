package com.smartlist.api.exceptions;

public class BadRequestException extends CustomException {
  public BadRequestException(String code, String message) {
    super(code, message);
  }
}
