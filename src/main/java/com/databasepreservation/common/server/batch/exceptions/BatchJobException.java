package com.databasepreservation.common.server.batch.exceptions;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BatchJobException extends Exception {
  public BatchJobException() {
    super();
  }

  public BatchJobException(String message) {
    super(message);
  }

  public BatchJobException(String message, Throwable cause) {
    super(message, cause);
  }

  public BatchJobException(Throwable cause) {
    super(cause);
  }

  protected BatchJobException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
