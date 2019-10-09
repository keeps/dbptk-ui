package com.databasepreservation.common.shared.client.tools;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class PathUtils {

  public static String getFileName(final String path) {
    int i = path.lastIndexOf("/");
    if (i == -1) {
      i = path.lastIndexOf("\\");
    }

    return path.substring(i+1);
  }
}
