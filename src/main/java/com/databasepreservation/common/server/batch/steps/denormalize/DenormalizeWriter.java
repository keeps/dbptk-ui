package com.databasepreservation.common.server.batch.steps.denormalize;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeWriter implements ItemWriter<DenormalizeProcessor.NestedDocumentWrapper> {
  private final DatabaseRowsSolrManager solrManager;
  private final String databaseUUID;

  public DenormalizeWriter(DatabaseRowsSolrManager solrManager, String databaseUUID) {
    this.solrManager = solrManager;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public void write(Chunk<? extends DenormalizeProcessor.NestedDocumentWrapper> chunk) throws Exception {

    for (DenormalizeProcessor.NestedDocumentWrapper item : chunk) {
      if (!item.getNestedDocuments().isEmpty()) {
        solrManager.addDatabaseField(databaseUUID, item.getParentUUID(), item.getNestedDocuments());
      }
    }
  }
}
