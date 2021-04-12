/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.denormalization;

import com.databasepreservation.common.client.models.structure.ViewerColumn;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RelatedColumnConfiguration implements Serializable {
    private String columnName;
    private String solrName;
    private int index;

    public RelatedColumnConfiguration(ViewerColumn column){
        index = column.getColumnIndexInEnclosingTable();
        solrName = column.getSolrName();
        columnName = column.getDisplayName();
    }

    public RelatedColumnConfiguration() {
    }

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
