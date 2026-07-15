/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.batch.exceptions;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DataTransformationException extends Exception {
  public DataTransformationException() {
    super();
  }

  public DataTransformationException(String message) {
    super(message);
  }

  public DataTransformationException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataTransformationException(Throwable cause) {
    super(cause);
  }

  protected DataTransformationException(String message, Throwable cause, boolean enableSuppression,
    boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
