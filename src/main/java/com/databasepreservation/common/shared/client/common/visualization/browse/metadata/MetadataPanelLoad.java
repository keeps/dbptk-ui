package com.databasepreservation.common.shared.client.common.visualization.browse.metadata;

import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSIARDBundle;

public abstract class MetadataPanelLoad {
  public abstract MetadataPanel load(ViewerDatabase database, ViewerSIARDBundle SIARDbundle);
}
