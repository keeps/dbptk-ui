package com.databasepreservation.common.server.batch.jobs;

import java.util.List;

import org.springframework.stereotype.Component;

import com.databasepreservation.common.server.batch.core.JobDefinition;
import com.databasepreservation.common.server.batch.core.StepDefinition;
import com.databasepreservation.common.server.batch.steps.denormalization.DenormalizationStep;
import com.databasepreservation.common.server.batch.steps.extraction.LobTextExtractionStep;
import com.databasepreservation.common.server.batch.steps.virtual.FinalizeVirtualEntitiesMetadataStep;
import com.databasepreservation.common.server.batch.steps.virtual.column.VirtualColumnStep;
import com.databasepreservation.common.server.batch.steps.virtual.reference.VirtualReferenceStep;
import com.databasepreservation.common.server.batch.steps.virtual.table.VirtualTableDeletionStep;
import com.databasepreservation.common.server.batch.steps.virtual.table.VirtualTableStep;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class DataTransformationJob implements JobDefinition {
  private final VirtualColumnStep virtualColumnStep;
  private final VirtualTableDeletionStep virtualTableDeletionStep;
  private final VirtualTableStep virtualTableStep;
  private final VirtualReferenceStep virtualReferenceStep;
  private final LobTextExtractionStep lobTextExtractionStep;
  private final DenormalizationStep denormalizationStep;
  private final FinalizeVirtualEntitiesMetadataStep finalizeMetadataStep;

  public DataTransformationJob(VirtualColumnStep virtualColumnStep, VirtualTableDeletionStep virtualTableDeletionStep,
    VirtualTableStep virtualTableStep, VirtualReferenceStep virtualReferenceStep,
    LobTextExtractionStep lobTextExtractionStep, DenormalizationStep denormalizationStep,
    FinalizeVirtualEntitiesMetadataStep finalizeMetadataStep) {
    this.virtualColumnStep = virtualColumnStep;
    this.virtualTableDeletionStep = virtualTableDeletionStep;
    this.virtualTableStep = virtualTableStep;
    this.virtualReferenceStep = virtualReferenceStep;
    this.lobTextExtractionStep = lobTextExtractionStep;
    this.denormalizationStep = denormalizationStep;
    this.finalizeMetadataStep = finalizeMetadataStep;
  }

  @Override
  public String getDisplayName() {
    return "Data Transformation";
  }

  @Override
  public List<StepDefinition> getSteps() {
    return List.of(virtualColumnStep, virtualTableDeletionStep, virtualTableStep, virtualReferenceStep,
      lobTextExtractionStep, denormalizationStep, finalizeMetadataStep);
  }
}
