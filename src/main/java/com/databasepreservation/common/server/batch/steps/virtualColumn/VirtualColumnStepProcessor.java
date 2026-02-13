package com.databasepreservation.common.server.batch.steps.virtualColumn;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
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
  private TableStatus table;

  private final Map<String, VirtualColumnStatus> virtualColumnStatusList = new HashMap<>();

  public VirtualColumnStepProcessor(CollectionStatus collectionStatus) {
    this.collectionStatus = collectionStatus;
  }

  @PostConstruct
  public void init() {
    LOGGER.info("Initializing processor for table: {}", tableId);

    table = collectionStatus.getTables().stream().filter(t -> t.getId().equals(tableId)).findFirst()
      .orElseThrow(() -> new RuntimeException("Table not found in configuration: " + tableId));

    if (table.getColumns() != null) {
      for (ColumnStatus column : table.getColumns()) {
        if (column.getType().equals(ViewerType.dbTypes.VIRTUAL)) {
          virtualColumnStatusList.put(column.getId(), column.getVirtualColumnStatus());
        }
      }
    }
  }

  @Override
  public ViewerRow process(ViewerRow row) throws Exception {
    LOGGER.debug("Processing row: {} for table: {}", row.getUuid(), tableId);

    Map<String, ViewerCell> cells = row.getCells();

    for (Map.Entry<String, VirtualColumnStatus> entry : virtualColumnStatusList.entrySet()) {
      String columnId = entry.getKey();
      VirtualColumnStatus virtualColumnStatus = entry.getValue();

      if (virtualColumnStatus.getProcessingState() == ProcessingState.TO_REMOVE) {
        // Mark the cell for removal by setting it to null
        cells.put(columnId, null);
      } else {
        // Apply the Handlebars template for the virtual column using the source columns
        String processedValue = HandlebarsUtils.applyVirtualColumnTemplate(row, table, virtualColumnStatus);
        // Create a new cell for the virtual column
        ViewerCell virtualCell = new ViewerCell();
        virtualCell.setValue(processedValue);
        cells.put(columnId, virtualCell);
      }
    }

    return row;
  }
}
