package com.databasepreservation.common.server.batch.steps.denormalization;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeStepWriter implements ItemWriter<DenormalizationStepProcessor.NestedDocumentWrapper> {
  private final DatabaseRowsSolrManager solrManager;
  private final String databaseUUID;

  public DenormalizeStepWriter(DatabaseRowsSolrManager solrManager, String databaseUUID) {
    this.solrManager = solrManager;
    this.databaseUUID = databaseUUID;
  }

  @Override
  public void write(Chunk<? extends DenormalizationStepProcessor.NestedDocumentWrapper> chunk) throws Exception {

    for (DenormalizationStepProcessor.NestedDocumentWrapper item : chunk) {
      if (item != null && !item.nestedDocuments().isEmpty()) {
        // Add the nested documents to Solr, associating them with the parent document's UUID
        solrManager.addDatabaseField(databaseUUID, item.parentUUID(), item.nestedDocuments());
      }
    }
  }
}
