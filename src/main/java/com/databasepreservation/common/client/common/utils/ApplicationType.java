/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.utils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ApplicationType {

  private static String type;

  public static String getType() {
    return type;
  }

  public static void setType(String type) {
    ApplicationType.type = type;
  }
}
