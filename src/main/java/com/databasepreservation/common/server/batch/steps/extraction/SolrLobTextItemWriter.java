package com.databasepreservation.common.server.batch.steps.extraction;

import java.util.Map;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SolrLobTextItemWriter implements ItemWriter<RowLobTextUpdate> {
  private final DatabaseRowsSolrManager solrManager;
  private final JobContext jobContext;

  public SolrLobTextItemWriter(DatabaseRowsSolrManager solrManager, JobContext jobContext) {
    this.solrManager = solrManager;
    this.jobContext = jobContext;
  }

  @Override
  public void write(Chunk<? extends RowLobTextUpdate> chunk) throws Exception {
    if (chunk == null || chunk.isEmpty()) {
      return;
    }

    String databaseUUID = jobContext.getDatabaseUUID();

    for (RowLobTextUpdate update : chunk.getItems()) {

      for (String columnId : update.getColumnsToClear()) {
        solrManager.clearExtractedLobTextField(databaseUUID, update.getUuid(), columnId);

        ColumnStatus columnStatus = jobContext.getCollectionStatus().getColumnByTableIdAndColumn(update.getTableId(),
          columnId);
        columnStatus.getLobTextExtractionStatus().setExtractedAndIndexedText(false);
      }

      for (Map.Entry<String, String> entry : update.getExtractedTexts().entrySet()) {
        String columnId = entry.getKey();
        String text = entry.getValue();

        solrManager.clearExtractedLobTextField(databaseUUID, update.getUuid(), columnId);
        solrManager.addExtractedTextField(databaseUUID, update.getUuid(), columnId, text);

        ColumnStatus columnStatus = jobContext.getCollectionStatus().getColumnByTableIdAndColumn(update.getTableId(),
          columnId);
        columnStatus.getLobTextExtractionStatus().setExtractedAndIndexedText(true);
      }
    }
  }
}
