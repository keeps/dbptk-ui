package com.databasepreservation.common.server.batch.components.writers;

import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SolrItemWriter<T extends IsIndexed> implements ItemWriter<T> {
  private final DatabaseRowsSolrManager solrManager;
  private final String databaseUUID;

  public SolrItemWriter(DatabaseRowsSolrManager solrManager, String databaseUUID) {
    this.solrManager = solrManager;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public void write(Chunk<? extends T> chunk) throws Exception {
    if (chunk == null || chunk.isEmpty()) {
      return;
    }

    List<? extends T> items = chunk.getItems();
    solrManager.insertBatchDocuments(databaseUUID, items);
  }
}
