package com.databasepreservation.common.server.batch.steps.virtual.table;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.steps.virtual.VirtualEntityStepUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualTableStepProcessor implements ItemProcessor<ViewerRow, ViewerRow> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VirtualTableStepProcessor.class);
  private final JobContext context;
  private final TableStatus tableStatus;

  public VirtualTableStepProcessor(JobContext context, String tableID) {
    this.context = context;
    this.tableStatus = VirtualEntityStepUtils.findTableStatus(context, tableID);
  }

  @Override
  public ViewerRow process(ViewerRow row) throws Exception {
    LOGGER.info("Processing virtual table {} for row: {}", tableStatus.getName(), row.getUuid());

    ViewerRow viewerRow = new ViewerRow();
    viewerRow.setUuid(tableStatus.getUuid() + "_" + row.getUuid());
    viewerRow.setDatabaseUUID(context.getDatabaseUUID());
    viewerRow.setTableUUID(tableStatus.getUuid());
    viewerRow.setTableId(tableStatus.getId());

    // only columns that are present in table status should be included in the
    // viewer row
    Map<String, ViewerCell> cellsToInclude = tableStatus.getColumns().stream()
      .filter(c -> row.getCells().containsKey(c.getId()))
      .collect(Collectors.toMap(c -> c.getId(), c -> row.getCells().get(c.getId())));

    viewerRow.setCells(cellsToInclude);

    return viewerRow;
  }
}
