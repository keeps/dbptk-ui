package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.tables;

import com.databasepreservation.main.common.shared.ViewerStructure.*;
import com.databasepreservation.main.desktop.client.common.EditableCell;
import com.databasepreservation.main.desktop.client.common.lists.MetadataTableList;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataEditPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import config.i18n.client.ClientMessages;

import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataConstraints implements MetadataEditPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ViewerSIARDBundle SIARDbundle;
  private ViewerTable table;
  private ViewerSchema schema;
  private String type = "constraint";

  MetadataConstraints(ViewerSIARDBundle SIARDbundle, ViewerSchema schema, ViewerTable table) {
    this.SIARDbundle = SIARDbundle;
    this.table = table;
    this.schema = schema;
  }

  @Override
  public MetadataTableList createTable() {

    List<ViewerCheckConstraint> columns = table.getCheckConstraints();
    Label header = new Label("");
    HTMLPanel info = new HTMLPanel(SafeHtmlUtils.EMPTY_SAFE_HTML);

    if (columns.isEmpty()) {
      return new MetadataTableList<>(header, messages.tableDoesNotContainConstraints());
    } else {

      return new MetadataTableList<>(header, info, columns.iterator(),
        new MetadataTableList.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerCheckConstraint>() {
          @Override
          public String getValue(ViewerCheckConstraint object) {
            return object.getName();
          }
        }), new MetadataTableList.ColumnInfo<>(messages.constraints_condition(), 15,
          new TextColumn<ViewerCheckConstraint>() {
            @Override
            public String getValue(ViewerCheckConstraint object) {
              return object.getCondition();
            }
          }),
        new MetadataTableList.ColumnInfo<>(messages.description(), 15, getDescriptionColumn()));
    }
  }

  @Override
  public Column<ViewerCheckConstraint, String> getDescriptionColumn() {
    Column<ViewerCheckConstraint, String> description = new Column<ViewerCheckConstraint, String>(new EditableCell()) {
      @Override
      public String getValue(ViewerCheckConstraint object) {
        return object.getDescription();
      }
    };

    description.setFieldUpdater((index, object, value) -> {
      object.setDescription(value);
      updateSIARDbundle(object.getName(), value);
    });

    return description;
  }

  @Override
  public void updateSIARDbundle(String name, String value) {
    SIARDbundle.setTableType(schema.getName(), table.getName(), type, name, value);
  }

}
