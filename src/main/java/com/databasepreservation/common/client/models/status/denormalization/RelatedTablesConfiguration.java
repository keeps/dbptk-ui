package com.databasepreservation.common.client.models.status.denormalization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RelatedTablesConfiguration implements Serializable {
    private String uuid;
    private int order;
    private String tableUUID;
    private String tableID;
    private String referencedTableUUID;
    private String referencedTableID;
    private List<ReferencesConfiguration> references;
    private List<RelatedColumnConfiguration> columnsIncluded;
    private DisplaySettingsConfiguration displaySettings;

    public RelatedTablesConfiguration(){
        references = new ArrayList<>();
        columnsIncluded = new ArrayList<>();
        displaySettings = new DisplaySettingsConfiguration();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTableUUID() {
        return tableUUID;
    }

    public void setTableUUID(String tableUUID) {
        this.tableUUID = tableUUID;
    }

    public String getTableID() {
        return tableID;
    }

    public void setTableID(String tableID) {
        this.tableID = tableID;
    }

    public String getReferencedTableUUID() {
        return referencedTableUUID;
    }

    public void setReferencedTableUUID(String referencedTableUUID) {
        this.referencedTableUUID = referencedTableUUID;
    }

    public String getReferencedTableID() {
        return referencedTableID;
    }

    public void setReferencedTableID(String referencedTableID) {
        this.referencedTableID = referencedTableID;
    }

    public List<ReferencesConfiguration> getReferences() {
        return references;
    }

    public void setReferences(List<ReferencesConfiguration> references) {
        this.references = references;
    }

    public List<RelatedColumnConfiguration> getColumnsIncluded() {
        return columnsIncluded;
    }

    public void setColumnsIncluded(List<RelatedColumnConfiguration> columnsIncluded) {
        this.columnsIncluded = columnsIncluded;
    }

    public DisplaySettingsConfiguration getDisplaySettings() {
        return displaySettings;
    }

    public void setDisplaySettings(DisplaySettingsConfiguration displaySettings) {
        this.displaySettings = displaySettings;
    }
}
