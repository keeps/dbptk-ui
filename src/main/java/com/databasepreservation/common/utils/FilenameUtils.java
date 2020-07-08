package com.databasepreservation.common.utils;

import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.tools.ViewerStringUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class FilenameUtils {

  public static String sanitizeFilename(String name) {
    return name.replaceAll("[:\\\\/*?|<>]", "_");
  }

  public static String getTemplateFilename(ViewerRow row, TableStatus configTable, ColumnStatus binaryColumn, String defaultValue) {
    String handlebarsFilename = HandlebarsUtils.applyExportTemplate(row, configTable,
        binaryColumn.getColumnIndex());
    if (ViewerStringUtils.isBlank(handlebarsFilename)) {
      handlebarsFilename = defaultValue;
    }

    return FilenameUtils.sanitizeFilename(handlebarsFilename);
  }
}
