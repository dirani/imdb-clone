package com.thecodinglab.imdbclone.exception;

public class BadRequestException extends RuntimeException {

  private String message;

  public BadRequestException(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
