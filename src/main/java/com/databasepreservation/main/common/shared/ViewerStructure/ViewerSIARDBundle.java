package com.databasepreservation.main.common.shared.ViewerStructure;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ViewerSIARDBundle implements IsSerializable {

  private Map<String, String> commandList = new HashMap<>();
  private static final String SEPARATOR = "---";
  private static final String DESCRIPTION = "description" + SEPARATOR;
  private static final String USER = "user:";
  private static final String ROLE = "role:";
  private static final String PRIVILEGES = "privileges:";
  private static final String SPACE = " ";
  private static final String TYPE = "type:";
  private static final String OBJECT = SPACE + "object:";
  private static final String GRANTOR = SPACE + "grantor:";
  private static final String GRANTEE = SPACE + "grantee:";
  private static final String SCHEMA = "schema:";
  private static final String TABLE = "table:";

  public ViewerSIARDBundle() {
  }

  public void setInformation(String key, String value) {
    commandList.put(key, value);
  }

  public String getInformation(String key) {
    return commandList.get(key);
  }

  public void setUser(String user, String description) {
    commandList.put(USER + user, DESCRIPTION + description);
  }

  public void setRole(String role, String description) {
    commandList.put(ROLE + role, DESCRIPTION + description);
  }

  public void setPrivileges(String type, String object, String grantor, String grantee, String description) {
    commandList.put(PRIVILEGES + "[" + TYPE + type + OBJECT + object + GRANTOR + grantor + GRANTEE + grantee + "]",
      DESCRIPTION + description);
  }
  
  public void setTable(String schema, String table, String description){
      commandList.put(SCHEMA + schema + SEPARATOR + TABLE + table, DESCRIPTION + description);
  }

  public void setTableType(String schema, String table, String type, String name, String description) {
    commandList.put(SCHEMA + schema + SEPARATOR + TABLE + table + SEPARATOR + type + ":" + name , DESCRIPTION + description);
  }

  public List<String> getCommandList() {
    List<String> bundleCommandList = new ArrayList<>();
    for (Map.Entry<String, String> entry : commandList.entrySet()) {
      bundleCommandList.add(entry.getKey() + SEPARATOR + entry.getValue() + SEPARATOR);
    }

    return bundleCommandList;
  }

  public void clearCommandList(){
      commandList.clear();
  }
}
