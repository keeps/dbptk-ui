package com.databasepreservation.common.utils;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;

import java.io.File;
import java.nio.file.Path;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class LobPathManager {
  public static String getZipFilePath(ViewerDatabase database, String tableUUID, int columnIndex, String rowIndex) {

    String siardTableFolder = database.getMetadata().getTable(tableUUID).getSiardName();
    String siardSchemaFolder = database.getMetadata().getSchemaFromTableUUID(tableUUID).getSiardName();
    String siardLobFolder = ViewerConstants.SIARD_LOB_FOLDER_PREFIX + (columnIndex+1);
    String recordFile = ViewerConstants.SIARD_RECORD_PREFIX + rowIndex + ViewerConstants.SIARD_LOB_FILE_EXTENSION;

    return "content" + "/" + siardSchemaFolder + "/" + siardTableFolder + "/" + siardLobFolder + "/" + recordFile;
  }

  public static Path getPath(ViewerAbstractConfiguration configuration, String databaseUUID, String tableUUID,
                             int columnIndex, String rowUUID) {
    Path tmpPath = configuration.getLobPath().resolve(databaseUUID).resolve(tableUUID)
      .resolve(String.valueOf(columnIndex));

    // example:
    // for uuid:
    // 123e4567-e89b-12d3-a456-426655440000
    // obtains the path:
    // homedir/lobs/<dbuuid>/<tableuuid>/<columnindex>/123e/4567/e89b/12d3/a456/426655440000.bin
    tmpPath = tmpPath.resolve("lob-" + rowUUID + ".bin");
    return tmpPath;
  }
}
