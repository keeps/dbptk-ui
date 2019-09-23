package com.databasepreservation.main.common.shared.client.tools;

import com.databasepreservation.main.common.shared.ViewerConstants;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ToolkitModuleName2ViewerModuleName {

  public static String transform(String moduleName) {
    switch (moduleName) {
      case "dbml": return "DBML";
      case "jdbc": return "JDBC";
      case "microsoft-access": return "Microsoft Access";
      case "mysql": return "MySQL";
      case "progress-openedge": return "Progress OpenEdge";
      case "oracle": return "Oracle";
      case "postgresql": return "PostgreSQL";
      case "microsoft-sql-server": return "SQL Server";
      case "sybase": return "Sybase";
      case ViewerConstants.SIARD1: return "SIARD 1";
      case ViewerConstants.SIARD2: return "SIARD 2";
      case ViewerConstants.SIARDDK: return "SIARD DK";
      default: return moduleName;
    }
  }
}
