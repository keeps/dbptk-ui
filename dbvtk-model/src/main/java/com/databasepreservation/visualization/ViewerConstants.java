package com.databasepreservation.visualization;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * non GWT-safe constants used in Database Viewer
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerConstants {
  /*
   * LOCAL USER DATA
   */
  public static final Path USER_DBVIEWER_DIR = Paths.get(System.getProperty("java.home"), ".db-visualization-toolkit");
}
