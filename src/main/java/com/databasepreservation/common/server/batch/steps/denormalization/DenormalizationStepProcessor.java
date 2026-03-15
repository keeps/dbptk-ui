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
    if (row.getNestedRowList() == null || row.getNestedRowList().isEmpty()) {
      return null;
    }

    return row;
  }

}
