package com.databasepreservation.common.server.jobs.quartz;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalize.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.transformers.Denormalize;
import com.databasepreservation.model.exception.ModuleException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

public class DenormalizeJob implements InterruptableJob {
  private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizeJob.class);

  private JobKey jobKey = null;
  private boolean interrupted = false;

  public void execute(JobExecutionContext context) throws JobExecutionException {

    jobKey = context.getJobDetail().getKey();
    ViewerDatabase database = (ViewerDatabase) context.getJobDetail().getJobDataMap().get("database");
    CollectionConfiguration configuration = (CollectionConfiguration) context.getJobDetail().getJobDataMap()
      .get("configuration");
    DenormalizeConfiguration denormalizeConfiguration = (DenormalizeConfiguration) context.getJobDetail()
      .getJobDataMap().get("denormalizeConfiguration");
    String tableId = (String) context.getJobDetail().getJobDataMap().get("tableId");

    LOGGER.info("init job " + jobKey + " for: " + tableId);
    try {
      new Denormalize(database, configuration, denormalizeConfiguration);
    } catch (ModuleException e) {
      throw new JobExecutionException(e);
    }

  }

  public void interrupt() throws UnableToInterruptJobException {
    interrupted = true;
  }
}
