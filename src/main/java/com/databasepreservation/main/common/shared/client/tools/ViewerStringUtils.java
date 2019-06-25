package com.databasepreservation.main.common.shared.client.tools;

/**
 * Utilities class to handle Strings. Can be used with/in GWT.
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerStringUtils {
  public static boolean isNotBlank(final CharSequence cs) {
    return !ViewerStringUtils.isBlank(cs);
  }

  public static boolean isBlank(final CharSequence cs) {
    return cs == null || cs.length() == 0;
  }
}
