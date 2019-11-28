package com.databasepreservation.common.client.models.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ViewerSIARDBundle implements Serializable {

  private Map<String, String> commandMap = new HashMap<>();
  private static final String SEPARATOR = "---";
  private static final String DESCRIPTION = "description" + SEPARATOR;
  private static final String USER = "user:";
  private static final String ROLE = "role:";
  private static final String PRIVILEGE = "privilege:";
  private static final String SPACE = " ";
  private static final String TYPE = "type:";
  private static final String OBJECT = SPACE + "object:";
  private static final String GRANTOR = SPACE + "grantor:";
  private static final String GRANTEE = SPACE + "grantee:";
  private static final String SCHEMA = "schema:";
  private static final String TABLE = "table:";
  private static final String VIEW = "view:";
  private static final String COLUMN = "column:";
  private static final String ROUTINE = "routine:";

  public ViewerSIARDBundle() {
  }

  public void setInformation(String key, String value) {
    commandMap.put(key, value);
  }

  public String getInformation(String key) {
    return commandMap.get(key);
  }

  public void setUser(String user, String description) {
    commandMap.put(USER + user, DESCRIPTION + description);
  }

  public void setRole(String role, String description) {
    commandMap.put(ROLE + role, DESCRIPTION + description);
  }

  public void setPrivileges(String type, String object, String grantor, String grantee, String description) {
    commandMap.put(PRIVILEGE + "[" + TYPE + type + OBJECT + object + GRANTOR + grantor + GRANTEE + grantee + "]",
      DESCRIPTION + description);
  }

  public void setTable(String schema, String table, String description) {
    commandMap.put(SCHEMA + schema + SEPARATOR + TABLE + table, DESCRIPTION + description);
  }

  public void setView(String schema, String view, String description) {
    commandMap.put(SCHEMA + schema + SEPARATOR + VIEW + view, DESCRIPTION + description);
  }

  public void setRoutine(String schema, String routine, String description) {
    commandMap.put(SCHEMA + schema + SEPARATOR + ROUTINE + routine, DESCRIPTION + description);
  }

  public void setTableType(String schema, String table, String type, String name, String description) {
    commandMap.put(SCHEMA + schema + SEPARATOR + TABLE + table + SEPARATOR + type + ":" + name,
      DESCRIPTION + description);
  }

  public void setViewColumn(String schema, String view, String name, String description) {
    commandMap.put(SCHEMA + schema + SEPARATOR + VIEW + view + SEPARATOR + COLUMN + name, DESCRIPTION + description);
  }

  public List<String> getCommandMapAsList() {
    List<String> bundleCommandList = new ArrayList<>();
    for (Map.Entry<String, String> entry : commandMap.entrySet()) {
      bundleCommandList.add(entry.getKey() + SEPARATOR + entry.getValue().replaceAll("\n", " ") + SEPARATOR);
    }

    return bundleCommandList;
  }

  public Map<String, String> getCommandMap() {
    return commandMap;
  }

  public void setCommandMap(Map<String, String> commandMap) {
    this.commandMap = commandMap;
  }

  public void clearCommandList() {
    commandMap.clear();
  }
}
