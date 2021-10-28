package com.ulashchick.podcast.common.exceptions;

public class ApplicationException extends RuntimeException {

  public ApplicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
