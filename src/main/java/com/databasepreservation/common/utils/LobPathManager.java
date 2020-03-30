package com.databasepreservation.common.utils;

import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class LobPathManager {
  public static String getZipFilePath(TableStatus configTable, int columnIndex, ViewerRow row) {
    String siardSchemaFolder = configTable.getSchemaFolder();
    String siardTableFolder = configTable.getTableFolder();
    String siardLobFolder = ViewerConstants.SIARD_LOB_FOLDER_PREFIX + (columnIndex + 1);

    String recordFile = "";

    Pattern p = Pattern.compile("\\d+");

    for (Map.Entry<String, ViewerCell> entry : row.getCells().entrySet()) {
      final Matcher matcher = p.matcher(entry.getKey());
      if (matcher.find()) {
        if (matcher.group().equals(Integer.toString(columnIndex))) {
          recordFile = entry.getValue().getValue();
        }
      }
    }

    return "content" + "/" + siardSchemaFolder + "/" + siardTableFolder + "/" + siardLobFolder + "/" + recordFile;
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
