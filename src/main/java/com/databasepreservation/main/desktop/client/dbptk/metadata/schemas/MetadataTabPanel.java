package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.google.gwt.user.cellview.client.Column;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface MetadataTabPanel<T> {

  MetadataTableList createTable(ViewerTable table, ViewerSchema schema);

  Column<T, ?> getDescriptionColumn(ViewerTable table, ViewerSchema schema);

  void updateSIARDbundle(String schemaName, String tableName, String displayName, String type, String value);
}
