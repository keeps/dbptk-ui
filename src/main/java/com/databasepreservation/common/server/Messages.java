/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public class Messages {
  private static final String MESSAGES_BUNDLE = "ServerMessages";
  private ResourceBundle resourceBundle;
  private Map<String, Map<String, ?>> translationsCache;

  public Messages(Locale locale, Path folder) {
    this.resourceBundle = ResourceBundle.getBundle(MESSAGES_BUNDLE, locale, new FolderBasedUTF8Control(folder));
    this.translationsCache = new HashMap<>();
  }

  /**
   * Get translation
   * 
   * @param key
   * @return
   */
  public String getTranslation(String key) {
    return resourceBundle.getString(key);
  }

  public String getTranslation(String key, String fallback) {
    String ret;
    try {
      ret = getTranslation(key);
    } catch (MissingResourceException e) {
      ret = fallback;
    }
    return ret;
  }

  public boolean containsTranslation(String key) {
    return resourceBundle.containsKey(key);
  }

  /**
   * 
   * prefix will be replaced by "i18n." for simplicity purposes
   */
  public <T extends Object> Map<String, T> getTranslations(String prefix, Class<T> valueClass,
    boolean replacePrefixFromKey) {
    // try cache first
    if (translationsCache.get(prefix) != null) {
      return (Map<String, T>) translationsCache.get(prefix);
    }

    Map<String, T> map = new HashMap<>();
    Enumeration<String> keys = resourceBundle.getKeys();
    String fullPrefix = prefix + ".";
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      if (key.startsWith(fullPrefix)) {
        map.put(replacePrefixFromKey ? key.replaceFirst(fullPrefix, "i18n.") : key,
          valueClass.cast(resourceBundle.getString(key)));
      }
    }

    // cache it
    translationsCache.put(prefix, map);
    return map;
  }

  private class FolderBasedUTF8Control extends Control {
    private static final String CONFIG_I18N_PATH = "/config/i18n/";
    private Path folder;

    public FolderBasedUTF8Control(Path folder) {
      this.folder = folder;
    }

    @Override
    public long getTimeToLive(String baseName, Locale locale) {
      // ask not to cache
      return TTL_DONT_CACHE;
    }

    public Locale getFallbackLocale(String baseName, Locale locale) {
      if (baseName == null) {
        throw new NullPointerException();
      }
      // 20160712 hsilva: the following line is needed otherwise default locale
      // is used and this can be incoherent with other parts of the code where
      // the default locale is ENGLISH
      Locale defaultLocale = Locale.ENGLISH;
      return locale.equals(defaultLocale) ? null : defaultLocale;
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
      throws IOException {

      if (!"java.properties".equals(format)) {
        return null;
      }

      String bundleName = toBundleName(baseName, locale) + ".properties";
      ResourceBundle bundle;
      InputStreamReader reader;

      File file = folder.resolve(bundleName).toFile();
      if (file.exists()) {
        try (InputStream is = new FileInputStream(file)) {
          reader = new InputStreamReader(is, StandardCharsets.UTF_8);
          bundle = new PropertyResourceBundle(reader);
          return bundle;
        }
      } else {
        try (InputStream is = this.getClass().getResourceAsStream(CONFIG_I18N_PATH + bundleName)) {
          reader = new InputStreamReader(is, StandardCharsets.UTF_8);
          bundle = new PropertyResourceBundle(reader);
          return bundle;
        }
      }
    }
  }
}
