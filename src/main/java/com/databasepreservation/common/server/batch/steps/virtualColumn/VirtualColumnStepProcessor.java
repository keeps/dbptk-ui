package com.databasepreservation.common.server.batch.steps.virtualColumn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerType;

import jakarta.annotation.PostConstruct;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnStepProcessor implements ItemProcessor<ViewerRow, ViewerRow> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VirtualColumnStepProcessor.class);
  private final CollectionStatus collectionStatus;

  @Value("#{stepExecutionContext['tableId']}")
  private String tableId;

  private final Map<String, VirtualColumnStatus> virtualColumnStatusList = new HashMap<>();

  public VirtualColumnStepProcessor(CollectionStatus collectionStatus) {
    this.collectionStatus = collectionStatus;
  }

  @PostConstruct
  public void init() {
    LOGGER.info("Initializing processor for table: {}", tableId);

    TableStatus table = collectionStatus.getTables().stream().filter(t -> t.getId().equals(tableId)).findFirst()
      .orElseThrow(() -> new RuntimeException("Table not found in configuration: " + tableId));

    if (table.getColumns() != null) {
      for (ColumnStatus column : table.getColumns()) {
        if (column.getType().equals(ViewerType.dbTypes.VIRTUAL)) {
          VirtualColumnStatus virtualColumnStatus = column.getVirtualColumnStatus();
          virtualColumnStatusList.put(column.getId(), virtualColumnStatus);
        }
      }
    }
  }

  @Override
  public ViewerRow process(ViewerRow row) throws Exception {
    LOGGER.info("Processing row: {} for table: {}", row.getUuid(), tableId);

    Map<String, ViewerCell> cells = row.getCells();

    for (Map.Entry<String, VirtualColumnStatus> entry : virtualColumnStatusList.entrySet()) {
      String columnId = entry.getKey();
      VirtualColumnStatus virtualColumnStatus = entry.getValue();

      // Extract and combine values from source columns
      String combinedValue = extractAndCombineValues(row, virtualColumnStatus.getSourceColumnsIds());

      // Create a new cell for the virtual column
      ViewerCell virtualCell = new ViewerCell();
      virtualCell.setValue(combinedValue);
      cells.put(columnId, virtualCell);
    }

    return row;
  }

  private String extractAndCombineValues(ViewerRow row, List<String> sourceIds) {
    StringBuilder combinedValue = new StringBuilder();

    for (String sourceId : sourceIds) {
      ViewerCell cell = row.getCells().get(sourceId);
      if (cell != null && cell.getValue() != null) {
        combinedValue.append(cell.getValue().toString()).append(" ");
      }
    }

    return combinedValue.toString().trim();
  }
}
