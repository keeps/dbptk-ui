package com.databasepreservation.common.server.batchv2.steps.virtualColumn;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import com.databasepreservation.common.api.utils.HandlebarsUtils;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.server.batchv2.common.TaskContext;

import jakarta.annotation.PostConstruct;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnStepProcessorV2 implements ItemProcessor<ViewerRow, ViewerRow> {
  private final TaskContext context;
  private final Map<String, VirtualColumnStatus> virtualColumns = new HashMap<>();
  private TableStatus tableStatus;

  @Value("#{stepExecutionContext['tableId']}")
  private String tableId;

  public VirtualColumnStepProcessorV2(TaskContext context) {
    this.context = context;
  }

  @PostConstruct
  public void init() {
    CollectionStatus status = context.getCollectionStatus();
    this.tableStatus = status.getTables().stream().filter(t -> t.getId().equals(tableId)).findFirst()
      .orElseThrow(() -> new RuntimeException("Tabela não encontrada no status: " + tableId));

    tableStatus.getColumns().stream().filter(c -> ViewerType.dbTypes.VIRTUAL.equals(c.getType()))
      .forEach(c -> virtualColumns.put(c.getId(), c.getVirtualColumnStatus()));
  }

  @Override
  public ViewerRow process(ViewerRow row) throws Exception {
    Map<String, ViewerCell> cells = row.getCells();

    for (Map.Entry<String, VirtualColumnStatus> entry : virtualColumns.entrySet()) {
      String columnId = entry.getKey();
      VirtualColumnStatus vcs = entry.getValue();

      if (vcs.getProcessingState() == ProcessingState.TO_REMOVE) {
        cells.put(columnId, null);
      } else {
        String processedValue = HandlebarsUtils.applyVirtualColumnTemplate(row, tableStatus, vcs);
        ViewerCell virtualCell = new ViewerCell();
        virtualCell.setValue(processedValue);
        cells.put(columnId, virtualCell);
      }
    }
    return row;
  }
}
