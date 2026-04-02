package com.databasepreservation.common.server.batch.jobs;

import java.util.List;

import org.springframework.stereotype.Component;

import com.databasepreservation.common.server.batch.core.JobDefinition;
import com.databasepreservation.common.server.batch.core.StepDefinition;
import com.databasepreservation.common.server.batch.steps.denormalization.DenormalizationCleanupStep;
import com.databasepreservation.common.server.batch.steps.denormalization.DenormalizationStep;
import com.databasepreservation.common.server.batch.steps.extraction.AsyncLobTextExtractionStep;
import com.databasepreservation.common.server.batch.steps.metadata.FinalizeSchemaMetadataStep;
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
  private final DenormalizationStep denormalizationStep;
  private final DenormalizationCleanupStep denormalizationCleanupStep;
  private final FinalizeSchemaMetadataStep finalizeMetadataStep;
  private final AsyncLobTextExtractionStep asyncLobTextExtractionStep;

  public DataTransformationJob(VirtualColumnStep virtualColumnStep, VirtualTableDeletionStep virtualTableDeletionStep,
    VirtualTableStep virtualTableStep, VirtualReferenceStep virtualReferenceStep,
    DenormalizationStep denormalizationStep, DenormalizationCleanupStep denormalizationCleanupStep,
    FinalizeSchemaMetadataStep finalizeMetadataStep, AsyncLobTextExtractionStep asyncLobTextExtractionStep) {
    this.virtualColumnStep = virtualColumnStep;
    this.virtualTableDeletionStep = virtualTableDeletionStep;
    this.virtualTableStep = virtualTableStep;
    this.virtualReferenceStep = virtualReferenceStep;
    this.denormalizationStep = denormalizationStep;
    this.denormalizationCleanupStep = denormalizationCleanupStep;
    this.finalizeMetadataStep = finalizeMetadataStep;
    this.asyncLobTextExtractionStep = asyncLobTextExtractionStep;
  }

  @Override
  public String getDisplayName() {
    return "Data Transformation";
  }

  @Override
  public List<StepDefinition> getSteps() {
    return List.of(virtualColumnStep, virtualTableDeletionStep, virtualTableStep, virtualReferenceStep,
      asyncLobTextExtractionStep, denormalizationStep, denormalizationCleanupStep, finalizeMetadataStep);
  }
}
