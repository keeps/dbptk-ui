package com.databasepreservation.common.server.batch.jobs;

import java.util.List;

import org.springframework.stereotype.Component;

import com.databasepreservation.common.server.batch.core.JobDefinition;
import com.databasepreservation.common.server.batch.core.StepDefinition;
import com.databasepreservation.common.server.batch.steps.denormalization.DenormalizationStep;
import com.databasepreservation.common.server.batch.steps.extraction.LobTextExtractionStep;
import com.databasepreservation.common.server.batch.steps.virtual.column.VirtualColumnStep;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class DataTransformationJob implements JobDefinition {
  private final VirtualColumnStep virtualColumnStep;
  private final LobTextExtractionStep lobTextExtractionStep;
  private final DenormalizationStep denormalizationStep;

  public DataTransformationJob(VirtualColumnStep virtualColumnStep, LobTextExtractionStep lobTextExtractionStep,
    DenormalizationStep denormalizationStep) {
    this.virtualColumnStep = virtualColumnStep;
    this.lobTextExtractionStep = lobTextExtractionStep;
    this.denormalizationStep = denormalizationStep;
  }

  @Override
  public String getDisplayName() {
    return "Data Transformation";
  }

  @Override
  public List<StepDefinition> getSteps() {
    return List.of(virtualColumnStep, lobTextExtractionStep, denormalizationStep);
  }
}
