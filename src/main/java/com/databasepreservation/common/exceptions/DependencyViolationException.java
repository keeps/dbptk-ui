/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
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
