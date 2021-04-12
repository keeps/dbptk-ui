/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.denormalization;

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
