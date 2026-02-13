package com.databasepreservation.common.server.batch.steps.denormalize;

import org.roda.core.data.exceptions.GenericException;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.steps.common.StepExecutionPolicy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class DenormalizationStepPolicy implements StepExecutionPolicy {

  @Override
  public boolean shouldExecute(String databaseUUID, CollectionStatus status) {
    if (status.getDenormalizations() == null || status.getDenormalizations().isEmpty()) {
      return false;
    }
    for (String entryID : status.getDenormalizations()) {
      try {
        DenormalizeConfiguration config = ViewerFactory.getConfigurationManager()
          .getDenormalizeConfigurationFromCollectionStatusEntry(databaseUUID, entryID);

        if (config != null && config.shouldProcess()) {
          return true;
        }
      } catch (GenericException e) {
        return false;
      }
    }

    return false;
  }
}
