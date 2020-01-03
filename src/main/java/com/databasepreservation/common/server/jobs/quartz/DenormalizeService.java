package com.databasepreservation.common.server.jobs.quartz;

import static org.quartz.TriggerBuilder.newTrigger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.TableConfiguration;
import com.databasepreservation.common.client.models.configuration.denormalize.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.model.exception.ModuleException;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DenormalizeService.class);
  private ViewerDatabase database;
  private CollectionConfiguration configuration;

  public DenormalizeService(String databaseUUID) throws ModuleException {
    try {
      database = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, databaseUUID);
      configuration = getConfiguration(Paths.get(databaseUUID + ViewerConstants.JSON_EXTENSION),
        CollectionConfiguration.class);
      setup();
    } catch (NotFoundException | GenericException e) {
      throw new ModuleException().withMessage("Cannot retrieved database from solr");
    }
  }

  private <T> T getConfiguration(Path path, Class<T> objectClass) throws ModuleException {
    Path configurationPath = ViewerConfiguration.getInstance().getDatabaseConfigPath().resolve(database.getUuid())
      .resolve(path);
    if (Files.exists(configurationPath)) {
      return JsonTransformer.readObjectFromFile(configurationPath, objectClass);
    } else {
      throw new ModuleException().withMessage("Configuration file not exist: " + configurationPath.toString());
    }
  }

  private void setup() throws ModuleException {
    for (TableConfiguration tableConfig : configuration.getTables()) {
      DenormalizeConfiguration denormalizeConfig = getConfiguration(
        Paths.get(tableConfig.getUuid() + "-CURRENT" + ViewerConstants.JSON_EXTENSION), DenormalizeConfiguration.class);
      if (denormalizeConfig != null) {
        run(denormalizeConfig);
      }
    }
  }

  private void run(DenormalizeConfiguration denormalizeConfiguration) throws ModuleException {
    SchedulerFactory sf = new StdSchedulerFactory();
    Scheduler sched = null;
    try {
      sched = sf.getScheduler();

      JobDetail job = JobBuilder.newJob(DenormalizeJob.class).withIdentity(denormalizeConfiguration.getTableID())
        .build();

      job.getJobDataMap().put("denormalizeConfiguration", denormalizeConfiguration);
      job.getJobDataMap().put("configuration", configuration);
      job.getJobDataMap().put("database", database);
      job.getJobDataMap().put("tableId", denormalizeConfiguration.getTableID());

      Trigger trigger = newTrigger().withIdentity(denormalizeConfiguration.getTableID()).startNow().build();

      sched.scheduleJob(job, trigger);
      sched.start();
    } catch (SchedulerException e) {
      throw new ModuleException().withMessage("Cannot retrieved database from solr");
    }
  }
}
