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
 * @TODO: Review nomenclatures
 */
public class ReferencesConfiguration implements Serializable {
  /** The target of the denormalization */
  private RelatedColumnConfiguration sourceTable;
  /** The source of the denormalization */
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
