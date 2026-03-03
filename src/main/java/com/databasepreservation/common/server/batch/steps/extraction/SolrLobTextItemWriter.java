package com.databasepreservation.common.server.batch.steps.extraction;

import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SolrLobTextItemWriter implements ItemWriter<RowLobTextUpdate> {
  private final DatabaseRowsSolrManager solrManager;
  private final String databaseUUID;

  public SolrLobTextItemWriter(DatabaseRowsSolrManager solrManager, String databaseUUID) {
    this.solrManager = solrManager;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public void write(Chunk<? extends RowLobTextUpdate> chunk) throws Exception {
    if (chunk == null || chunk.isEmpty()) {
      return;
    }

    for (RowLobTextUpdate update : chunk.getItems()) {

      for (String columnId : update.getColumnsToClear()) {
        solrManager.clearExtractedLobTextField(databaseUUID, update.getUuid(), columnId);
      }

      for (Map.Entry<String, String> entry : update.getExtractedTexts().entrySet()) {
        String columnId = entry.getKey();
        String text = entry.getValue();

        solrManager.clearExtractedLobTextField(databaseUUID, update.getUuid(), columnId);
        solrManager.addExtractedTextField(databaseUUID, update.getUuid(), columnId, text);
      }
    }
  }
}
