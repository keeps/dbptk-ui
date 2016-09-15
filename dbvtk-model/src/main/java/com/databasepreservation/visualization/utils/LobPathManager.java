package com.databasepreservation.visualization.utils;

import java.io.File;
import java.nio.file.Path;

import com.databasepreservation.visualization.ViewerConstants;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class LobPathManager {
  public static Path getPath(String tableUUID, int columnIndex, String rowUUID) {
    Path tmpPath = ViewerConstants.getWorkspaceForLobs().resolve(tableUUID).resolve(String.valueOf(columnIndex));

    // example:
    // for uuid:
    // 123e4567-e89b-12d3-a456-426655440000
    // obtains the path:
    // homedir/lobs/<tableuuid>/<columnindex>/123e/4567/e89b/12d3/a456/426655440000.bin
    tmpPath = tmpPath.resolve(rowUUID.substring(0, 4) + File.separatorChar
      + rowUUID.substring(4).replace('-', File.separatorChar) + ".bin");
    return tmpPath;
  }
}
