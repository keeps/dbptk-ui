package com.databasepreservation.common.client.tools;

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

  public static String concat(final String ... values) {
    String concat = "";
    for (String value : values) {
      concat = concat.concat(value).concat("_");
    }

    return concat;
  }

  // method to prettify method names
  public static String getPrettifiedActionMethod(String actionMethod) {
    String method = actionMethod.substring(0, 1).toUpperCase() + actionMethod.substring(1);
    method = method.replaceAll("([A-Z])", " $1").trim();
    return method.replaceAll("S I A R D", "SIARD");
  }
}
