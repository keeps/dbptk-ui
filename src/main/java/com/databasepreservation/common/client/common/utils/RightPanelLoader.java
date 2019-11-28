package com.databasepreservation.common.client.common.utils;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.common.RightPanel;

/**
 * Main passes an instance of this class to DatabasePanel, so it can obtain a
 * RightPanel when convenient
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class RightPanelLoader {
  public abstract RightPanel load(ViewerDatabase database);
}
