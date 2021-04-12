/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
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

  public static String concat(final String... values) {
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

  public static String removeWhiteSpaces(String target) {
    return target.replaceAll("\\s", "");
  }

  public static String replaceAllFor(String target, String regexToReplace, String replacement) {
    return target.replaceAll(regexToReplace, replacement);
  }

  public static String addStringOnPosition(String str, String ch, int position) {
    StringBuilder sb = new StringBuilder(str);
    sb.insert(position, ch);
    return sb.toString();
  }
}
