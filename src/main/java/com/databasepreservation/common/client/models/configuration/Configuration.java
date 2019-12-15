package com.databasepreservation.common.client.models.configuration;

import com.databasepreservation.common.client.models.configuration.denormalize.DenormalizeConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class Configuration implements Serializable {
    private String version;
    private String databaseUUID;
    private List<DenormalizeConfiguration> DenormalizeList = new ArrayList<>();

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDatabaseUUID() {
        return databaseUUID;
    }

    public void setDatabaseUUID(String databaseUUID) {
        this.databaseUUID = databaseUUID;
    }

    @JsonProperty("denormalize")
    public List<DenormalizeConfiguration> getDenormalizeList() {
        return DenormalizeList;
    }

    public void setDenormalizeList(List<DenormalizeConfiguration> denormalizeList) {
        DenormalizeList = denormalizeList;
    }
}
