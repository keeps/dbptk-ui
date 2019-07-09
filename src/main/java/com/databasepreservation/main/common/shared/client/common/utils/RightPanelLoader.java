package com.databasepreservation.main.common.shared.client.common.utils;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.client.common.RightPanel;

import java.util.Map;

/**
 * Main passes an instance of this class to DatabasePanel, so it can obtain a
 * RightPanel when convenient
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class RightPanelLoader {
  public abstract RightPanel load(ViewerDatabase database, Map<String, String> SIARDbundle);
}
