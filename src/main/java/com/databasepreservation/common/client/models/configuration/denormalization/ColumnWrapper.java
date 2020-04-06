package com.databasepreservation.common.client.models.configuration.denormalization;

import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ColumnWrapper {
  private String uuid;
  private String referencedTableName;
  private String columnDisplayName;
  private String columnDescription;
  private RelatedTablesConfiguration relatedTable;
  private DenormalizeConfiguration configuration;
  private ViewerMetadata metadata;

  public ColumnWrapper(String referencedTableName, DenormalizeConfiguration configuration, ViewerMetadata metadata) {
    this(null, referencedTableName, null, configuration, metadata);
  }

  public ColumnWrapper(String uuid, String referencedTableName, RelatedTablesConfiguration relatedTablesConfiguration,
    DenormalizeConfiguration configuration, ViewerMetadata metadata) {
    this.uuid = uuid;
    this.referencedTableName = referencedTableName;
    this.relatedTable = relatedTablesConfiguration;
    this.configuration = configuration;
    this.metadata = metadata;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getReferencedTableName() {
    return referencedTableName;
  }

  public void setReferencedTableName(String referencedTableName) {
    this.referencedTableName = referencedTableName;
  }

  public String getColumnDisplayName() {
    return columnDisplayName;
  }

  public void setColumnDisplayName(String columnDisplayName) {
    if (this.columnDisplayName == null || this.columnDisplayName.isEmpty()) {
      this.columnDisplayName = columnDisplayName;
    } else {
      this.columnDisplayName = this.columnDisplayName + ", " + columnDisplayName;
    }
  }

  public String getColumnDescription() {
    return columnDescription;
  }

  public void setColumnDescription(String columnDescription) {
    if (this.columnDescription == null || this.columnDescription.isEmpty()) {
      this.columnDescription = columnDescription;
    } else {
      this.columnDescription = this.columnDescription + ", " + columnDescription;
    }
  }

  public RelatedTablesConfiguration getRelatedTable() {
    return relatedTable;
  }

  public void setRelatedTable(RelatedTablesConfiguration relatedTable) {
    this.relatedTable = relatedTable;
  }

  public SafeHtml createPath() {
    SafeHtmlBuilder sb = new SafeHtmlBuilder();
    sb.appendHtmlConstant("<span class=\"table-ref-link\">");
    createPathRelatedTo(relatedTable, sb);
    sb.appendHtmlConstant("<span class=\"table-ref-path\"><b>");
    // sb.appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.BREADCRUMB_SEPARATOR));
    sb.appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.COLUMN));
    sb.append(SafeHtmlUtils.fromString(columnDisplayName));
    sb.appendHtmlConstant("</b></span>");
    sb.appendHtmlConstant("</span>");
    relatedTable.setPath(sb.toSafeHtml().asString());
    return sb.toSafeHtml();
  }

  private void createPathRelatedTo(RelatedTablesConfiguration relatedTable, SafeHtmlBuilder sb) {
    if (!relatedTable.getReferencedTableID().equals(configuration.getTableID())) {
      for (RelatedTablesConfiguration table : configuration.getRelatedTables()) {
        boolean innerPath = false;
        if (table.getTableID().equals(relatedTable.getReferencedTableID())) {
          createPathRelatedTo(table, sb);
          break;
        }

        for (RelatedTablesConfiguration innerRelatedTable : table.getRelatedTables()) {
          if (innerRelatedTable.getTableID().equals(relatedTable.getReferencedTableID())) {
            createPathRelatedTo(innerRelatedTable, sb);
            innerPath = true;
            break;
          }
        }
        if (innerPath) {
          break;
        }
      }
    }

    sb.appendHtmlConstant("<span class=\"table-ref-path\">");
    for (ReferencesConfiguration reference : relatedTable.getReferences()) {
      sb.appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.COLUMN));
      sb.append(SafeHtmlUtils.fromString(reference.getReferencedTable().getColumnName()));
    }

    sb.appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.BREADCRUMB_SEPARATOR));
    sb.appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE));
    sb.append(SafeHtmlUtils.fromString(metadata.getTable(relatedTable.getTableUUID()).getName()));
    sb.appendHtmlConstant("</span>");
  }
}
