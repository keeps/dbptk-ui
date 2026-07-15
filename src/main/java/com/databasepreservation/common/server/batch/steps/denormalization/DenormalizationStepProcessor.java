/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.batch.steps.denormalization;

import org.springframework.batch.item.ItemProcessor;

import com.databasepreservation.common.client.models.structure.ViewerRow;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizationStepProcessor implements ItemProcessor<ViewerRow, ViewerRow> {

  public DenormalizationStepProcessor() {
  }

  @Override
  public ViewerRow process(ViewerRow row) throws Exception {
    if (row.getNestedRowList() == null) {
      row.setNestedRowList(new java.util.ArrayList<>());
    }

    return row;
  }

}
