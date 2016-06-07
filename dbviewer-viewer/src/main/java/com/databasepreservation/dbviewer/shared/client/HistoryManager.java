package com.databasepreservation.dbviewer.shared.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class HistoryManager {
  public static final String ROUTE_DATABASE = "database";
  public static final String ROUTE_SCHEMA = "schema";
  public static final String ROUTE_TABLE = "table";
  public static final String ROUTE_RECORD = "record";
  public static final String ROUTE_REFERENCES = "references";

  public static final String HISTORY_SEP = "/";
  public static final String HISTORY_SEP_REGEX = "/";
  public static final String HISTORY_SEP_ESCAPE = "%2F";

  public static void gotoRoot() {
    newHistory(new ArrayList<String>());
  }

  public static void gotoDatabaseList() {
    newHistory(Arrays.asList(ROUTE_DATABASE));
  }

  public static void gotoDatabase(String databaseUUID) {
    newHistory(Arrays.asList(ROUTE_DATABASE, databaseUUID));
  }

  public static void gotoSchema(String databaseUUID, String schemaUUID) {
    newHistory(Arrays.asList(ROUTE_SCHEMA, databaseUUID, schemaUUID));
  }

  public static void gotoTable(String databaseUUID, String tableUUID) {
    newHistory(Arrays.asList(ROUTE_TABLE, databaseUUID, tableUUID));
  }

  public static void gotoRecord(String databaseUUID, String tableUUID, String recordUUID) {
    newHistory(Arrays.asList(ROUTE_RECORD, databaseUUID, tableUUID, recordUUID));
  }

  public static void gotoReferences(String databaseUUID, String tableUUID, String recordUUID, String columnIndexInTable) {
    newHistory(Arrays.asList(ROUTE_REFERENCES, databaseUUID, tableUUID, recordUUID, columnIndexInTable));
  }

  private static void newHistory(List<String> path) {
    // History.newItem(createHistoryToken(path)
    String hash = createHistoryToken(path);
    Window.Location.assign("#" + hash);
  }

  private static String createHistoryToken(List<String> tokens) {
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (String token : tokens) {
      if (first) {
        first = false;
      } else {
        builder.append(HISTORY_SEP);
      }

      String encodedToken = URL.encode(token).replaceAll(HISTORY_SEP_REGEX, HISTORY_SEP_ESCAPE);
      builder.append(encodedToken);
    }

    return builder.toString();
  }

  public static List<String> getCurrentHistoryPath() {
    List<String> tokens = new ArrayList<>();

    String hash = Window.Location.getHash();
    if (hash.length() > 0) {
      hash = hash.substring(1);
      List<String> split = Arrays.asList(hash.split(HISTORY_SEP_REGEX));
      for (String item : split) {
        tokens.add(URL.decode(item));
      }
    }

    return tokens;
  }

  public static String linkToReferences(String database_uuid, String table_uuid, String record_uuid,
    String columnIndexInTable) {
    return createHistoryToken(Arrays.asList(ROUTE_REFERENCES, database_uuid, table_uuid, record_uuid));
  }

  public static String linkToRecord(String database_uuid, String table_uuid, String record_uuid) {
    return createHistoryToken(Arrays.asList(ROUTE_TABLE, database_uuid, table_uuid, record_uuid));
  }

  public static String linkToTable(String database_uuid, String table_uuid) {
    return createHistoryToken(Arrays.asList(ROUTE_TABLE, database_uuid, table_uuid));
  }

  public static String linkToDatabase(String database_uuid) {
    return createHistoryToken(Arrays.asList(ROUTE_DATABASE, database_uuid));
  }

  public static String linkToSchema(String database_uuid, String schema_uuid) {
    return createHistoryToken(Arrays.asList(ROUTE_SCHEMA, database_uuid, schema_uuid));
  }
}
