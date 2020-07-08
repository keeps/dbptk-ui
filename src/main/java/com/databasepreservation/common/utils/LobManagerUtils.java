package com.databasepreservation.common.utils;

import java.nio.file.Path;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class LobManagerUtils {

  public static boolean isLobEmbedded(TableStatus config, ViewerRow row, int columnIndex) {
    final String lobCellValue = getLobCellValue(config, row, columnIndex);

    return lobCellValue.startsWith(ViewerConstants.SIARD_EMBEDDED_LOB_PREFIX);
  }

  public static String getLobCellValue(TableStatus config, ViewerRow row, int columnIndex) {
    return row.getCells().get(config.getColumnByIndex(columnIndex).getId()).getValue();
  }

  public static String getDefaultFilename(String index) {
    return ViewerConstants.SIARD_RECORD_PREFIX + index + ViewerConstants.SIARD_LOB_FILE_EXTENSION;
  }

  public static String getZipFilePath(TableStatus configTable, int columnIndex, ViewerRow row) {
    String siardSchemaFolder = configTable.getSchemaFolder();
    String siardTableFolder = configTable.getTableFolder();
    String siardLobFolder = ViewerConstants.SIARD_LOB_FOLDER_PREFIX + (columnIndex + 1);

    final String lobCellValue = LobManagerUtils.getLobCellValue(configTable, row, columnIndex);

    return "content" + "/" + siardSchemaFolder + "/" + siardTableFolder + "/" + siardLobFolder + "/" + lobCellValue;
  }

  public static Path getConsolidatedPath(ViewerAbstractConfiguration configuration, String databaseUUID, String tableUUID,
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
