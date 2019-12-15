package com.databasepreservation.common.client.models.configuration.denormalize;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DisplaySettingsConfiguration implements Serializable {
    private String type;
    private String displayFormat;
    private String nestedSolrName;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayFormat() {
        return displayFormat;
    }

    public void setDisplayFormat(String displayFormat) {
        this.displayFormat = displayFormat;
    }

    public String getNestedSolrName() {
        return nestedSolrName;
    }

    public void setNestedSolrName(String nestedSolrName) {
        this.nestedSolrName = nestedSolrName;
    }
}
