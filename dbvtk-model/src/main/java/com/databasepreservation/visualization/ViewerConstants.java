package com.databasepreservation.visualization;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Non GWT-safe constants used in Database Viewer.
 *
 * @see com.databasepreservation.visualization.shared.ViewerSafeConstants for
 *      the GWT-safe constants
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerConstants {
  /*
   * LOCAL USER DATA
   */
  public static final Path USER_DBVIEWER_DIR = Paths.get(System.getProperty("user.home"), ".db-visualization-toolkit");
}
