package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.tables;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerCandidateKey;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.MetadataTabPanel;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataCandidateKeys implements MetadataTabPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private ViewerSIARDBundle SIARDbundle;
  private ViewerDatabase database;

  MetadataCandidateKeys(ViewerSIARDBundle SIARDbundle, ViewerDatabase database) {
    this.SIARDbundle = SIARDbundle;
    this.database = database;
  }

  @Override
  public MetadataTableList createTable(ViewerTable table, ViewerSchema schema) {
    List<ViewerCandidateKey> columns = table.getCandidateKeys();
    Label header = new Label("");
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    if (columns.isEmpty()) {
      return new MetadataTableList<>(header, messages.tableDoesNotContainCandidateKeys());
    } else {
      return new MetadataTableList<>(header, info, columns.iterator(),
        new MetadataTableList.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerCandidateKey>() {
          @Override
          public String getValue(ViewerCandidateKey object) {
            return object.getName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.columnName(), 15,
          new TextColumn<ViewerCandidateKey>() {
            @Override
            public String getValue(ViewerCandidateKey object) {
              List<Integer> columnsIndex = object.getColumnIndexesInViewerTable();
              List<ViewerColumn> tableColumns = table.getColumns();
              List<String> columnsName = new ArrayList<>();
              for (Integer index : columnsIndex) {
                columnsName.add(tableColumns.get(index).getDisplayName());
              }

              return columnsName.toString();
            }
          }),
        new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn(table, schema)));
    }
  }

  @Override
  public Column<ViewerCandidateKey, String> getDescriptionColumn(ViewerTable table, ViewerSchema schema) {
    Column<ViewerCandidateKey, String> description = new Column<ViewerCandidateKey, String>(new EditTextCell()) {
      @Override
      public String getValue(ViewerCandidateKey object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      updateSIARDbundle(schema.getName(), table.getName(), "candidateKey", object.getName(), value);
    });

    return description;
  }

  @Override
  public void updateSIARDbundle(String schemaName, String tableName, String type, String displayName, String value) {
    SIARDbundle.setTableType(schemaName, tableName, type, displayName, value);
  }
}
