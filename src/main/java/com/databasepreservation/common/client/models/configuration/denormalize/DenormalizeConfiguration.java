package com.databasepreservation.common.client.models.configuration.denormalize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.models.structure.ViewerTable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeConfiguration implements Serializable {
    private String uuid;
    private String version;
    private String state;
    private String job;
    private String tableUUID;
    private String tableID;
    private List<RelatedTablesConfiguration> relatedTables;

    public DenormalizeConfiguration(){
    }

    public DenormalizeConfiguration(String databaseUUID, ViewerTable table){
        setUuid(databaseUUID + "." + table.getUuid());
        setTableUUID(table.getUuid());
        setTableID(table.getId());
        relatedTables = new ArrayList<>();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
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

    public List<RelatedTablesConfiguration> getRelatedTables() {
        return relatedTables;
    }

    public void setRelatedTables(List<RelatedTablesConfiguration> relatedTables) {
        this.relatedTables = relatedTables;
    }
}
