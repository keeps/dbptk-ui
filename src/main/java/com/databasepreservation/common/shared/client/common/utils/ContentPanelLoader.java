package com.databasepreservation.common.shared.client.common.utils;

import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.common.ContentPanel;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public abstract class ContentPanelLoader {
    public abstract ContentPanel load(ViewerDatabase database);
}
