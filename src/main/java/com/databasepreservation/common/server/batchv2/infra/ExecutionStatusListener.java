package com.databasepreservation.common.server.batchv2.infra;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.server.batchv2.common.TaskContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ExecutionStatusListener implements StepExecutionListener {
  private final TaskContext context;

  public ExecutionStatusListener(TaskContext context) {
    this.context = context;
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    ExecutionContext executionContext = stepExecution.getExecutionContext();

    Map<String, Object> executionParameters = new HashMap<>();

    if (executionContext.containsKey("tableId")) {
      Object tableId = executionContext.get("tableId");
      if (tableId != null) {
        executionParameters.put("tableId", tableId);
      }
    }

    if (executionContext.containsKey("columnId")) {
      Object columnId = executionContext.get("columnId");
      if (columnId != null) {
        executionParameters.put("columnId", columnId);
      }
    }

    if (!executionParameters.isEmpty()) {
      context.updateExecutionStatus(stepExecution.getStepName(), executionParameters,
        stepExecution.getExitStatus().getExitCode());
    }

    return stepExecution.getExitStatus();
  }
}
