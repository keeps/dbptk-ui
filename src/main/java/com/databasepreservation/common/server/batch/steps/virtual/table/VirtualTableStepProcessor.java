package com.databasepreservation.common.server.batch.steps.virtual.table;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerMimeType;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.batch.context.JobContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualTableStepProcessor implements ItemProcessor<ViewerRow, ViewerRow> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VirtualTableStepProcessor.class);
  private final JobContext context;
  private final TableStatus tableStatus;

  public VirtualTableStepProcessor(JobContext context, String tableID) {
    this.context = context;
    this.tableStatus = context.getCollectionStatus().findTableStatusById(tableID);
    LOGGER.info("Initialized VirtualTableStepProcessor for table: {} with virtual table status: {}", tableID,
      tableStatus != null ? "found" : "not found");
  }

  @Override
  public ViewerRow process(ViewerRow row) throws Exception {
    LOGGER.debug("Processing virtual table {} for row: {}", tableStatus.getName(), row.getUuid());

    ViewerRow viewerRow = new ViewerRow();
    viewerRow.setUuid(tableStatus.getUuid() + "_" + row.getUuid());
    viewerRow.setDatabaseUUID(context.getDatabaseUUID());
    viewerRow.setTableUUID(tableStatus.getUuid());
    viewerRow.setTableId(tableStatus.getId());

    Map<String, ViewerCell> cellsToInclude = new HashMap<>();

    tableStatus.getColumns().forEach(c -> {
      String colId = c.getId();

      if (row.getCells().containsKey(colId)) {
        ViewerCell originalCell = row.getCells().get(colId);
        cellsToInclude.put(colId, originalCell);

        if (originalCell.getStoreType() != null) {
          viewerRow.getColsLobTypeList().put(colId, originalCell.getStoreType());
        }

        String mimeType = originalCell.getMimeType();
        String fileExt = originalCell.getFileExtension();

        if (mimeType != null && !mimeType.equals("null")) {
          ViewerMimeType mimeObj = new ViewerMimeType();
          mimeObj.setMimeType(mimeType);
          mimeObj.setFileExtension(fileExt);

          viewerRow.getColsMimeTypeList().put(colId, mimeObj);
        }
      }
    });

    viewerRow.setCells(cellsToInclude);

    return viewerRow;
  }
}
