package com.databasepreservation.common.server.batch.steps.virtual.reference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.StepDefinition;
import com.databasepreservation.common.server.batch.core.TaskletStepDefinition;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.batch.steps.virtual.VirtualEntityStepUtils;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class VirtualReferenceStep implements StepDefinition, TaskletStepDefinition {

  @Autowired
  private DatabaseRowsSolrManager solrManager;

  @Override
  public String getDisplayName() {
    return "Virtual References Processing";
  }

  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return new ExecutionPolicy() {
      @Override
      public boolean shouldExecute(JobContext context) {
        CollectionStatus status = context.getCollectionStatus();
        if (status.getTables() == null) {
          return false;
        }

        return status.getTables().stream().anyMatch(VirtualEntityStepUtils::hasVirtualReferencesToProcess);
      }
    };
  }

  @Override
  public ErrorPolicy getErrorPolicy() {
    ErrorPolicy policy = new ErrorPolicy(0, 2);
    policy.getRetryableExceptions().add(SolrServerException.class);
    policy.getRetryableExceptions().add(IOException.class);
    return policy;
  }

  @Override
  public Tasklet createTasklet(JobContext context, ExecutionContext executionContext) {
    return (contribution, chunkContext) -> {

      String databaseUUID = context.getDatabaseUUID();
      ViewerDatabase database = solrManager.retrieve(ViewerDatabase.class, databaseUUID);
      ViewerMetadata metadata = database.getMetadata();
      boolean requiresMetadataUpdate = false;

      for (TableStatus tableStatus : context.getCollectionStatus().getTables()) {
        if (tableStatus.getForeignKeys() == null)
          continue;

        ViewerTable sourceViewerTable = metadata.getTable(tableStatus.getUuid());
        if (sourceViewerTable == null)
          continue;

        for (ForeignKeysStatus fkStatus : tableStatus.getForeignKeys()) {
          if (!ViewerType.dbTypes.VIRTUAL.equals(fkStatus.getType()) || fkStatus.getVirtualForeignKeysStatus() == null)
            continue;

          ProcessingState state = fkStatus.getVirtualForeignKeysStatus().getProcessingState();

          if (state == ProcessingState.TO_PROCESS || state == ProcessingState.TO_REMOVE) {

            if (sourceViewerTable.getForeignKeys() != null) {
              sourceViewerTable.getForeignKeys().removeIf(fk -> fk.getName().equals(fkStatus.getName()));
            } else {
              sourceViewerTable.setForeignKeys(new ArrayList<>());
            }

            if (state == ProcessingState.TO_PROCESS) {
              ViewerTable targetViewerTable = metadata.getTable(fkStatus.getReferencedTableUUID());
              if (targetViewerTable != null) {
                ViewerForeignKey newFk = buildViewerForeignKey(fkStatus, sourceViewerTable, targetViewerTable);
                sourceViewerTable.getForeignKeys().add(newFk);
              }
            }

            requiresMetadataUpdate = true;
          }
        }
      }

      if (requiresMetadataUpdate) {
        solrManager.updateDatabaseMetadata(databaseUUID, metadata);
      }

      return org.springframework.batch.repeat.RepeatStatus.FINISHED;
    };
  }

  @Override
  public void onStepCompleted(JobContext context, BatchStatus status) throws BatchJobException {
    if (status == BatchStatus.COMPLETED) {
      boolean needsSave = false;

      for (TableStatus tableStatus : context.getCollectionStatus().getTables()) {
        if (tableStatus.getForeignKeys() != null) {

          boolean removed = tableStatus.getForeignKeys()
            .removeIf(fk -> ViewerType.dbTypes.VIRTUAL.equals(fk.getType()) && fk.getVirtualForeignKeysStatus() != null
              && fk.getVirtualForeignKeysStatus().getProcessingState() == ProcessingState.TO_REMOVE);

          if (removed)
            needsSave = true;

          for (ForeignKeysStatus fk : tableStatus.getForeignKeys()) {
            if (ViewerType.dbTypes.VIRTUAL.equals(fk.getType()) && fk.getVirtualForeignKeysStatus() != null
              && fk.getVirtualForeignKeysStatus().getProcessingState() == ProcessingState.TO_PROCESS) {

              fk.getVirtualForeignKeysStatus().setProcessingState(ProcessingState.PROCESSED);
              fk.getVirtualForeignKeysStatus().setLastExecutionDate(new Date());
              needsSave = true;
            }
          }
        }
      }

      if (needsSave) {
        try {
          ViewerFactory.getConfigurationManager().updateCollectionStatus(context.getDatabaseUUID(),
            context.getCollectionStatus());
        } catch (Exception e) {
          throw new BatchJobException("Failed to save CollectionStatus to disk after completing VirtualReferenceStep",
            e);
        }
      }
    }
  }

  private ViewerForeignKey buildViewerForeignKey(ForeignKeysStatus fkStatus, ViewerTable sourceTable,
    ViewerTable targetTable) {
    ViewerForeignKey vfk = new ViewerForeignKey();
    vfk.setName(fkStatus.getName());
    vfk.setReferencedTableUUID(fkStatus.getReferencedTableUUID());
    vfk.setReferencedTableId(fkStatus.getReferencedTableId());
    vfk.setDescription("Virtual Reference created via UI");

    List<ViewerReference> references = new ArrayList<>();

    if (fkStatus.getReferences() != null) {
      for (ForeignKeysStatus.ReferencedColumnStatus refStatus : fkStatus.getReferences()) {
        ViewerColumn sourceCol = sourceTable.getColumnBySolrName(refStatus.getSourceColumnId());
        ViewerColumn targetCol = targetTable.getColumnBySolrName(refStatus.getReferencedColumnId());

        if (sourceCol != null && targetCol != null) {
          ViewerReference ref = new ViewerReference();
          ref.setSourceColumnIndex(sourceCol.getColumnIndexInEnclosingTable());
          ref.setReferencedColumnIndex(targetCol.getColumnIndexInEnclosingTable());
          references.add(ref);
        }
      }
    }

    vfk.setReferences(references);
    return vfk;
  }
}
