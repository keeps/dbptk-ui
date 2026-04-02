package com.databasepreservation.common.server.batch.steps.extraction;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class LobTextExtractionStepExecutionPolicy implements ExecutionPolicy {
  @Override
  public boolean shouldExecute(JobContext context) {
    CollectionStatus status = context.getCollectionStatus();

    if (status == null || status.getTables() == null) {
      return false;
    }

    return status.getTables().stream().anyMatch(LobTextExtractionStepUtils::hasLobsToProcess);
  }

  @Override
  public int getChunkSize() {
    return 10;
  }

  @Override
  public int getConcurrencyLimit() {
    return 3;
  }
}
