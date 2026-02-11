package com.databasepreservation.common.server.batch.steps.common.writers;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SolrItemWriter implements ItemWriter<ViewerRow> {
  private final DatabaseRowsSolrManager solrManager;
  private final String databaseUUID;

  public SolrItemWriter(DatabaseRowsSolrManager solrManager, String databaseUUID) {
    this.solrManager = solrManager;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public void write(Chunk<? extends ViewerRow> chunk) throws Exception {
    for (ViewerRow viewerRow : chunk) {
      solrManager.updateRow(databaseUUID, viewerRow);
    }
  }
}
