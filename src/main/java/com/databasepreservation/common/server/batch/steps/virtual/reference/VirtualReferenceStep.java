package com.databasepreservation.common.server.batch.steps.virtual.reference;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.StepDefinition;
import com.databasepreservation.common.server.batch.core.TaskletStepDefinition;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.batch.steps.virtual.VirtualSchemaBuilderUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class VirtualReferenceStep implements StepDefinition, TaskletStepDefinition {

  @Override
  public String getDisplayName() {
    return "Virtual References Processing";
  }

  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return context -> {
      if (context.getCollectionStatus().getTables() == null) {
        return false;
      }

      return context.getCollectionStatus().getTables().stream().anyMatch(TableStatus::hasVirtualReferencesToProcess);
    };
  }

  @Override
  public ErrorPolicy getErrorPolicy() {
    return new ErrorPolicy(0, 0);
  }

  @Override
  public Tasklet createTasklet(JobContext context, ExecutionContext executionContext) {
    if (context.getCollectionStatus().getTables() != null) {
      context.getCollectionStatus().getTables().forEach(tableStatus -> {
        if (tableStatus.getForeignKeys() != null) {
          tableStatus.getForeignKeys().stream().filter(fk -> fk.isVirtual() && fk.getVirtualForeignKeysStatus() != null)
            .filter(fk -> fk.getVirtualForeignKeysStatus().shouldProcess())
            .filter(fk -> !fk.getVirtualForeignKeysStatus().isMarkedForRemoval())
            .forEach(fk -> fk.getVirtualForeignKeysStatus().markAsPendingMetadata());

          context.changeViewerDatabase(database -> {
            ViewerMetadata metadata = database.getMetadata();
            ViewerTable sourceViewerTable = metadata.getTable(tableStatus.getUuid());
            if (sourceViewerTable != null) {
              if (sourceViewerTable.getForeignKeys() == null) {
                sourceViewerTable.setForeignKeys(new java.util.ArrayList<>());
              }

              tableStatus.getForeignKeys().stream().filter(fk -> fk.isVirtual()
                && fk.getVirtualForeignKeysStatus() != null && fk.getVirtualForeignKeysStatus().isPendingMetadata())
                .forEach(fkStatus -> {
                  sourceViewerTable.getForeignKeys().removeIf(fk -> fk.getName().equals(fkStatus.getName()));
                  ViewerTable targetTable = metadata.getTable(fkStatus.getReferencedTableUUID());
                  if (targetTable != null) {
                    sourceViewerTable.getForeignKeys()
                      .add(VirtualSchemaBuilderUtils.buildViewerForeignKey(fkStatus, sourceViewerTable, targetTable));
                  }
                });
            }
          });
        }
      });
    }
    return (contribution, chunkContext) -> RepeatStatus.FINISHED;
  }

  @Override
  public void onStepCompleted(JobContext context, BatchStatus status) throws BatchJobException {
    // No action needed on step completion, as all processing is done in the
    // FinalizeVirtualEntitiesMetadataStep
  }
}
