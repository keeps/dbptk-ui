package com.databasepreservation.common.shared.client.common.utils;

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
