package com.databasepreservation.common.client.models.wizard;

import com.databasepreservation.common.client.models.wizard.connection.ConnectionParameters;
import com.databasepreservation.common.client.models.wizard.customViews.CustomViewsParameters;
import com.databasepreservation.common.client.models.wizard.export.ExportOptionsParameters;
import com.databasepreservation.common.client.models.wizard.export.MetadataExportOptionsParameters;
import com.databasepreservation.common.client.models.wizard.table.TableAndColumnsParameters;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class CreateSIARDParameters implements Serializable {
  private ConnectionParameters connectionParameters;
  private TableAndColumnsParameters tableAndColumnsParameters;
  private CustomViewsParameters customViewsParameters;
  private ExportOptionsParameters exportOptionsParameters;
  private MetadataExportOptionsParameters metadataExportOptionsParameters;
  private String uniqueID;

  public CreateSIARDParameters() {}

  public CreateSIARDParameters(ConnectionParameters connectionParameters,
    TableAndColumnsParameters tableAndColumnsParameters, CustomViewsParameters customViewsParameters,
    ExportOptionsParameters exportOptionsParameters, MetadataExportOptionsParameters metadataExportOptionsParameters) {
    this.connectionParameters = connectionParameters;
    this.tableAndColumnsParameters = tableAndColumnsParameters;
    this.customViewsParameters = customViewsParameters;
    this.exportOptionsParameters = exportOptionsParameters;
    this.metadataExportOptionsParameters = metadataExportOptionsParameters;
  }

  public CreateSIARDParameters(ConnectionParameters connectionParameters,
                               TableAndColumnsParameters tableAndColumnsParameters, CustomViewsParameters customViewsParameters,
    ExportOptionsParameters exportOptionsParameters, MetadataExportOptionsParameters metadataExportOptionsParameters,
    String uniqueID) {
    this.connectionParameters = connectionParameters;
    this.tableAndColumnsParameters = tableAndColumnsParameters;
    this.customViewsParameters = customViewsParameters;
    this.exportOptionsParameters = exportOptionsParameters;
    this.metadataExportOptionsParameters = metadataExportOptionsParameters;
    this.uniqueID = uniqueID;
  }

  public ConnectionParameters getConnectionParameters() {
    return connectionParameters;
  }

  public void setConnectionParameters(ConnectionParameters connectionParameters) {
    this.connectionParameters = connectionParameters;
  }

  public TableAndColumnsParameters getTableAndColumnsParameters() {
    return tableAndColumnsParameters;
  }

  public void setTableAndColumnsParameters(TableAndColumnsParameters tableAndColumnsParameters) {
    this.tableAndColumnsParameters = tableAndColumnsParameters;
  }

  public CustomViewsParameters getCustomViewsParameters() {
    return customViewsParameters;
  }

  public void setCustomViewsParameters(CustomViewsParameters customViewsParameters) {
    this.customViewsParameters = customViewsParameters;
  }

  public ExportOptionsParameters getExportOptionsParameters() {
    return exportOptionsParameters;
  }

  public void setExportOptionsParameters(ExportOptionsParameters exportOptionsParameters) {
    this.exportOptionsParameters = exportOptionsParameters;
  }

  public MetadataExportOptionsParameters getMetadataExportOptionsParameters() {
    return metadataExportOptionsParameters;
  }

  public void setMetadataExportOptionsParameters(MetadataExportOptionsParameters metadataExportOptionsParameters) {
    this.metadataExportOptionsParameters = metadataExportOptionsParameters;
  }

  public String getUniqueID() {
    return uniqueID;
  }

  public void setUniqueID(String uniqueID) {
    this.uniqueID = uniqueID;
  }
}
