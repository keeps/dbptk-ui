package com.databasepreservation.common.client.common.visualization.preferences;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class LocalPreferencesManager {
  private static final String PREFIX = "dbptk_prefs_";

  interface PrefsMapper extends ObjectMapper<LocalColumnPreferences> {
  }

  private static final PrefsMapper mapper = GWT.create(PrefsMapper.class);

  public static LocalColumnPreferences getColumnPreferences(String dbUuid, String tableId, String columnSolrName) {
    Storage storage = Storage.getLocalStorageIfSupported();
    if (storage != null) {
      String json = storage.getItem(generateKey(dbUuid, tableId, columnSolrName));
      if (json != null && !json.isEmpty()) {
        try {
          return mapper.read(json);
        } catch (Exception e) {
          GWT.log("Failed to parse local preferences", e);
        }
      }
    }
    return new LocalColumnPreferences();
  }

  public static void saveColumnPreferences(String dbUuid, String tableId, String columnSolrName,
    LocalColumnPreferences prefs) {
    Storage storage = Storage.getLocalStorageIfSupported();
    if (storage != null) {
      try {
        String json = mapper.write(prefs);
        storage.setItem(generateKey(dbUuid, tableId, columnSolrName), json);
      } catch (Exception e) {
        GWT.log("Failed to write local preferences", e);
      }
    }
  }

  public static void clearTablePreferences(String dbUuid, String tableId) {
    Storage storage = Storage.getLocalStorageIfSupported();
    if (storage != null) {
      String prefixMatch = PREFIX + dbUuid + "_" + tableId + "_";
      for (int i = storage.getLength() - 1; i >= 0; i--) {
        String key = storage.key(i);
        if (key != null && key.startsWith(prefixMatch)) {
          storage.removeItem(key);
        }
      }
    }
  }

  private static String generateKey(String dbUuid, String tableId, String columnSolrName) {
    return PREFIX + dbUuid + "_" + tableId + "_" + columnSolrName;
  }
}
