package com.ulashchick.dashboard.common.exceptions;

public class ApplicationException extends RuntimeException{

  public ApplicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
