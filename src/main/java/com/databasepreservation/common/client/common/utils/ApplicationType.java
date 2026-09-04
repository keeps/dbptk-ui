/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.utils;

import com.databasepreservation.common.client.ClientConfigurationManager;
import com.databasepreservation.common.client.ViewerConstants;

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

  public static boolean isDesktopForWeb() {
    return ClientConfigurationManager.getBoolean(false, ViewerConstants.ENABLE_DESKTOP_IN_BROWSER_ENVIRONMENT);
  }

  public static boolean isDesktop() {
    return getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP) && !isDesktopForWeb();
  }

  public static boolean isServer() {
    return !isDesktop() && !isDesktopForWeb();
  }
}
