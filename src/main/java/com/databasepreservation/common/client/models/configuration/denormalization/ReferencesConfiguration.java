package com.databasepreservation.common.client.models.configuration.denormalization;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ReferencesConfiguration implements Serializable {
    private RelatedColumnConfiguration sourceTable;
    private RelatedColumnConfiguration referencedTable;

    public RelatedColumnConfiguration getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(RelatedColumnConfiguration sourceTable) {
        this.sourceTable = sourceTable;
    }

    public RelatedColumnConfiguration getReferencedTable() {
        return referencedTable;
    }

    public void setReferencedTable(RelatedColumnConfiguration referencedTable) {
        this.referencedTable = referencedTable;
    }
}
