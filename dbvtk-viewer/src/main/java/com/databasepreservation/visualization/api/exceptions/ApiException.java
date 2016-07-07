package com.databasepreservation.visualization.api.exceptions;

import com.databasepreservation.visualization.exceptions.ViewerException;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ApiException extends ViewerException {
  private static final long serialVersionUID = 4667937307148805083L;

  public static final int INVALID_PARAMETER_VALUE = 1;
  public static final int EMPTY_PARAMETER = 2;
  public static final int RESOURCE_ALREADY_EXISTS = 3;

  private int code;

  public ApiException(int code, String msg) {
    super(msg);
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
