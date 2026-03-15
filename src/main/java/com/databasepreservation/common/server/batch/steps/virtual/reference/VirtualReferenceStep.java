package com.databasepreservation.common.server.batch.steps.virtual.reference;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.StepDefinition;
import com.databasepreservation.common.server.batch.core.TaskletStepDefinition;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ErrorPolicy;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;

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
    // All metadata mutation logic has been moved to the
    // FinalizeVirtualEntitiesMetadataStep
    // We keep this Tasklet returning FINISHED just for visual feedback and history
    // purposes in Spring
    return (contribution, chunkContext) -> org.springframework.batch.repeat.RepeatStatus.FINISHED;
  }

  @Override
  public void onStepCompleted(JobContext context, BatchStatus status) throws BatchJobException {
    // No action needed on step completion, as all processing is done in the
    // FinalizeVirtualEntitiesMetadataStep
  }
}
