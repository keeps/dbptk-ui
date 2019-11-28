package com.databasepreservation.common.client.common.visualization.metadata;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;

public abstract class MetadataPanelLoad {
  public abstract MetadataPanel load(ViewerDatabase database, ViewerSIARDBundle SIARDbundle);
}
