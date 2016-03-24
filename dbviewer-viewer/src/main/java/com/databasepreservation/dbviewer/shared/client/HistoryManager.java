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
  public static final String REST_DATABASE = "database";
  public static final String REST_TABLE = "table";

  public static final String HISTORY_SEP = "/";
  public static final String HISTORY_SEP_REGEX = "/";
  public static final String HISTORY_SEP_ESCAPE = "%2F";

  public static void gotoRoot() {
    newHistory(new ArrayList<String>());
  }

  public static void gotoDatabaseList() {
    newHistory(Arrays.asList(REST_DATABASE));
  }

  public static void gotoDatabase(String databaseId) {
    newHistory(Arrays.asList(REST_DATABASE, databaseId));
  }

  public static void gotoTable(String databaseId, String tableId) {
    newHistory(Arrays.asList(REST_TABLE, databaseId, tableId));
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
}
