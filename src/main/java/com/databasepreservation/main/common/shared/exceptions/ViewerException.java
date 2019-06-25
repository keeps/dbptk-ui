package com.databasepreservation.main.common.shared.exceptions;

import com.databasepreservation.model.exception.ModuleException;

/**
 * Handles viewer-related exceptions. Extends ModuleException from dbptk-model
 * for convenience.
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerException extends ModuleException {
  /**
   * Create an empty generic module exception
   */
  public ViewerException() {
    super();
  }

  /**
   * Create a generic module exception specifying the cause
   *
   * @param cause
   *          the underlying error
   */
  public ViewerException(Throwable cause) {
    withCause(cause);
  }
  /**
   * Create a generic module exception
   *
   * @param mesg
   *          the error message
   */
  public ViewerException(String mesg) {
    withMessage(mesg);
  }

  /**
   * Create a generic module exception specifying a message and the cause
   *
   * @param message
   *          the error message
   * @param cause
   */
  public ViewerException(String message, Throwable cause) {
    withCause(cause).withMessage(message);
  }
}
