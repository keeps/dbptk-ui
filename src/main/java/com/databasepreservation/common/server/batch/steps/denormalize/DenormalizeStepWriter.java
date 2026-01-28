package com.databasepreservation.common.server.batch.steps.denormalize;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeStepWriter implements ItemWriter<DenormalizeStepProcessor.NestedDocumentWrapper> {
  private final DatabaseRowsSolrManager solrManager;
  private final String databaseUUID;

  public DenormalizeStepWriter(DatabaseRowsSolrManager solrManager, String databaseUUID) {
    this.solrManager = solrManager;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public void write(Chunk<? extends DenormalizeStepProcessor.NestedDocumentWrapper> chunk) throws Exception {

    for (DenormalizeStepProcessor.NestedDocumentWrapper item : chunk) {
      if (!item.nestedDocuments().isEmpty()) {
        solrManager.addDatabaseField(databaseUUID, item.parentUUID(), item.nestedDocuments());
      }
    }
  }
}
