package com.databasepreservation.common.server.v2batch.steps.virtualColumn;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.server.v2batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.v2batch.exceptions.DataTransformationException;
import com.databasepreservation.common.server.v2batch.job.JobContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnStepProcessor implements ItemProcessor<ViewerRow, ViewerRow> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VirtualColumnStepProcessor.class);

  private final Map<String, VirtualColumnStatus> virtualColumns;
  private final TableStatus tableStatus;

  public VirtualColumnStepProcessor(JobContext context, String tableId) {
    this.tableStatus = context.getCollectionStatus().getTables().stream().filter(t -> t.getId().equals(tableId))
      .findFirst().orElse(null);

    if (tableStatus != null) {
      this.virtualColumns = tableStatus.getColumns().stream()
        .filter(c -> ViewerType.dbTypes.VIRTUAL.equals(c.getType()))
        .collect(Collectors.toMap(c -> c.getId(), c -> c.getVirtualColumnStatus()));
    } else {
      this.virtualColumns = new HashMap<>();
    }
  }

  @Override
  public ViewerRow process(ViewerRow row) throws Exception {
    if (tableStatus == null) {
      throw new BatchJobException("Table with ID " + row.getTableId()
        + " not found in collection status. Cannot process virtual columns for row: " + row.getUuid());
    }

    try {
      processVirtualColumns(row);
      return row;
    } catch (Exception e) {
      LOGGER.error("Error processing virtual columns for row: {}", row.getUuid(), e);
      throw new DataTransformationException("Failed to process virtual columns for row: " + row.getUuid(), e);
    }
  }

  private void processVirtualColumns(ViewerRow row) {
    Map<String, ViewerCell> cells = row.getCells();

    for (Map.Entry<String, VirtualColumnStatus> entry : virtualColumns.entrySet()) {
      String columnId = entry.getKey();
      VirtualColumnStatus vcs = entry.getValue();

      if (isMarkedForRemoval(vcs)) {
        cells.put(columnId, null);
      } else {
        renderVirtualColumn(row, columnId, vcs, cells);
      }
    }
  }

  private void renderVirtualColumn(ViewerRow row, String columnId, VirtualColumnStatus vcs,
    Map<String, ViewerCell> cells) {
    String processedValue = HandlebarsUtils.applyVirtualColumnTemplate(row, tableStatus, vcs);

    ViewerCell virtualCell = new ViewerCell();
    virtualCell.setValue(processedValue);
    cells.put(columnId, virtualCell);
  }

  private boolean isMarkedForRemoval(VirtualColumnStatus vcs) {
    return vcs != null && vcs.getProcessingState() == ProcessingState.TO_REMOVE;
  }
}
