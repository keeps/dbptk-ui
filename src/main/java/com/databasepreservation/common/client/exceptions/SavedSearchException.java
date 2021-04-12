/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.exceptions;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SavedSearchException extends Exception {
  public SavedSearchException() {
  }

  public SavedSearchException(String message, Throwable cause) {
    super(message, cause);
  }

}
