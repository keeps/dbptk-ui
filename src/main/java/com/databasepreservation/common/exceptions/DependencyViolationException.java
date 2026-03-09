package com.databasepreservation.common.exceptions;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DependencyViolationException extends Exception {

  public DependencyViolationException() {
    super();
  }

  public DependencyViolationException(String message) {
    super(message);
  }

  public DependencyViolationException(String message, Throwable cause) {
    super(message, cause);
  }

  public DependencyViolationException(Throwable cause) {
    super(cause);
  }

  protected DependencyViolationException(String message, Throwable cause, boolean enableSuppression,
    boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
