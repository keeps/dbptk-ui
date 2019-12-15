package com.databasepreservation.common.client.models.configuration.denormalize;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RelatedColumnConfiguration implements Serializable {
    private String columnName;
    private String solrName;
    private int index;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getSolrName() {
        return solrName;
    }

    public void setSolrName(String solrName) {
        this.solrName = solrName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
