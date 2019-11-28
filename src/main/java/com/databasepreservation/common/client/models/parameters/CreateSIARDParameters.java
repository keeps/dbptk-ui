package com.databasepreservation.common.client.models.parameters;

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

  public ConnectionParameters getConnectionParameters() {
    return connectionParameters;
  }

  public TableAndColumnsParameters getTableAndColumnsParameters() {
    return tableAndColumnsParameters;
  }

  public CustomViewsParameters getCustomViewsParameters() {
    return customViewsParameters;
  }

  public ExportOptionsParameters getExportOptionsParameters() {
    return exportOptionsParameters;
  }

  public MetadataExportOptionsParameters getMetadataExportOptionsParameters() {
    return metadataExportOptionsParameters;
  }
}
