/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1.utils;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class StringResponse implements Serializable {
  @Serial
  private static final long serialVersionUID = -2652807260459802093L;

  private String value;

  public StringResponse() {
    // empty constructor
  }

  public StringResponse(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
