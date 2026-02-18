package com.databasepreservation.common.server.v2batch.steps.denormalization;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizationStepWriter implements ItemWriter<DenormalizationStepProcessor.NestedDocumentWrapper> {
  private final DatabaseRowsSolrManager solrManager;
  private final String databaseUUID;

  public DenormalizationStepWriter(DatabaseRowsSolrManager solrManager, String databaseUUID) {
    this.solrManager = solrManager;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public void write(Chunk<? extends DenormalizationStepProcessor.NestedDocumentWrapper> chunk) throws Exception {

    for (DenormalizationStepProcessor.NestedDocumentWrapper item : chunk) {
      if (!item.nestedDocuments().isEmpty()) {
        solrManager.addDatabaseField(databaseUUID, item.parentUUID(), item.nestedDocuments());
      }
    }
  }
}
