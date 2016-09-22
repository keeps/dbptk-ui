package com.databasepreservation.visualization;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;

/**
 * Non GWT-safe constants used in Database Viewer.
 *
 * @see com.databasepreservation.visualization.shared.ViewerSafeConstants for
 *      the GWT-safe constants
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerConstants {
  private static final String ENV_DBVTK_WORKSPACE = "DBVTK_WORKSPACE";
  private static final String PROP_DBVTK_WORKSPACE = "dbvtk.workspace";

  private static Path workspaceDirectory = null;

  public static Path getWorkspaceDirectory() {
    if (workspaceDirectory == null) {
      String property = System.getProperty(PROP_DBVTK_WORKSPACE);
      String env = System.getenv(ENV_DBVTK_WORKSPACE);

      if (StringUtils.isNotBlank(property)) {
        workspaceDirectory = Paths.get(property);
      } else if (StringUtils.isNotBlank(env)) {
        workspaceDirectory = Paths.get(env);
      } else {
        workspaceDirectory = Paths.get(System.getProperty("user.home"), ".db-visualization-toolkit");
      }
    }
    return workspaceDirectory;
  }

  private static Path workspaceForLobs = null;

  public static Path getWorkspaceForLobs() {
    if (workspaceForLobs == null) {
      workspaceForLobs = getWorkspaceDirectory().resolve("lobs");
    }
    return workspaceForLobs;
  }
}
