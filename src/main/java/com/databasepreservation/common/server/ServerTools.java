/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package com.databasepreservation.common.server;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerTools {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerTools.class);

  /**
   * Parse a locale string into a Locale
   * 
   * @param localeString
   *          the locale string, e.g. en_US
   * @return
   */
  public static Locale parseLocale(String localeString) {
    Locale locale = null;
    if (StringUtils.isNotBlank(localeString)) {
      String[] localeArgs = localeString.split("_");

      if (localeArgs.length == 1) {
        locale = new Locale(localeArgs[0]);
      } else if (localeArgs.length == 2) {
        locale = new Locale(localeArgs[0], localeArgs[1]);
      } else if (localeArgs.length == 3) {
        locale = new Locale(localeArgs[0], localeArgs[1], localeArgs[2]);
      }
    } else {
      locale = Locale.ENGLISH;
    }

    return locale;
  }
}
