package com.databasepreservation.main.desktop.client.dbptk.metadata;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;

import java.util.Map;

public abstract class MetadataPanelLoad {
    public abstract MetadataPanel load(ViewerDatabase database, Map<String, String> SIARDbundle);
}
