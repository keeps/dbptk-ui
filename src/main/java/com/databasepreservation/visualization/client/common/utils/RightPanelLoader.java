package com.databasepreservation.visualization.client.common.utils;

import com.databasepreservation.visualization.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.browse.RightPanel;

/**
 * Main passes an instance of this class to DatabasePanel, so it can obtain a
 * RightPanel when convenient
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class RightPanelLoader {
  public abstract RightPanel load(ViewerDatabase database);
}
