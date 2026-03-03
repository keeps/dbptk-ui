package com.databasepreservation.common.server.batch.steps.extraction;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.databasepreservation.common.server.batch.core.BatchConstants;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.server.batch.components.readers.SolrItemReader;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.core.AbstractIndexingStepDefinition;
import com.databasepreservation.common.server.batch.core.PartitionableStep;
import com.databasepreservation.common.server.batch.exceptions.BatchJobException;
import com.databasepreservation.common.server.batch.policy.ExecutionPolicy;
import com.databasepreservation.common.server.batch.steps.partition.PartitionStrategy;
import com.databasepreservation.common.server.batch.steps.partition.TablePartitionStrategy;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Component
public class LobTextExtractionStep extends AbstractIndexingStepDefinition<ViewerRow, RowLobTextUpdate>
  implements PartitionableStep {

  private final Map<String, FileSystem> partitionResources = new ConcurrentHashMap<>();

  @Override
  public String getDisplayName() {
    return "Lob Text Extraction";
  }

  @Override
  public ExecutionPolicy getExecutionPolicy() {
    return new LobTextExtractionStepExecutionPolicy();
  }

  @Override
  public PartitionStrategy getPartitionStrategy() {
    return new TablePartitionStrategy(solrManager, LobTextExtractionStepUtils::hasLobsToProcess);
  }

  @Override
  public long calculateWorkload(JobContext context) {
    return calculatePartitionedWorkload(context);
  }

  @Override
  public void onPartitionStarted(JobContext jobContext, ExecutionContext stepContext) throws BatchJobException {
    String tableId = stepContext.getString(BatchConstants.TABLE_ID_KEY);
    String dbVersion = stepContext.getString(BatchConstants.DB_VERSION_KEY);
    String dbPath = stepContext.getString(BatchConstants.DB_PATH_KEY);

    if (!LobTextExtractionStepUtils.isDKVersion(dbVersion)) {
      try {
        FileSystem fs = FileSystems.newFileSystem(Path.of(dbPath));
        partitionResources.put(tableId, fs);
      } catch (IOException e) {
        throw new BatchJobException("Failed to open SIARD file for extraction", e);
      }
    }
  }

  @Override
  public ItemReader<ViewerRow> createReader(JobContext context, ExecutionContext stepContext) {
    String tableId = stepContext.getString(BatchConstants.TABLE_ID_KEY);
    Filter filter = (Filter) stepContext.get(BatchConstants.FILTER_KEY);

    TableStatus tableStatus = context.getCollectionStatus().getTables().stream().filter(t -> t.getId().equals(tableId))
      .findFirst().orElse(null);

    List<String> fieldsToReturn = new ArrayList<>();
    fieldsToReturn.add(ViewerConstants.INDEX_ID);
    if (tableStatus != null) {
      tableStatus.getColumns().stream().filter(LobTextExtractionStepUtils::shouldProcess).map(ColumnStatus::getId)
        .forEach(fieldsToReturn::add);
    }

    return new SolrItemReader<>(solrManager, context.getDatabaseUUID(), filter, fieldsToReturn, ViewerRow.class);
  }

  @Override
  public ItemProcessor<ViewerRow, RowLobTextUpdate> createProcessor(JobContext context,
    ExecutionContext partitionContext) {
    String tableId = partitionContext.getString(BatchConstants.TABLE_ID_KEY);
    String dbVersion = partitionContext.getString(BatchConstants.DB_VERSION_KEY);
    FileSystem fs = partitionResources.get(tableId);
    return new LobTextExtractionProcessor(context, tableId, fs, dbVersion);
  }

  @Override
  public ItemWriter<RowLobTextUpdate> createWriter(JobContext context) {
    return new SolrLobTextItemWriter(solrManager, context.getDatabaseUUID());
  }

  @Override
  public void onPartitionCompleted(JobContext jobContext, ExecutionContext partitionContext, BatchStatus status)
    throws BatchJobException {
    String tableId = partitionContext.getString(BatchConstants.TABLE_ID_KEY);
    FileSystem fs = partitionResources.remove(tableId);
    if (fs != null && fs.isOpen()) {
      try {
        fs.close();
      } catch (IOException e) {
        throw new BatchJobException("Failed to close SIARD FileSystem for table " + tableId, e);
      }
    }

    if (status == BatchStatus.COMPLETED) {
      TableStatus tableStatus = jobContext.getCollectionStatus().getTables().stream()
        .filter(t -> t.getId().equals(tableId)).findFirst().orElse(null);

      if (tableStatus != null) {
        LobTextExtractionStepUtils.updateProcessedColumnsStateInMemory(tableStatus);
      }
    }
  }

  @Override
  public void onStepCompleted(JobContext jobContext, BatchStatus status) throws BatchJobException {
  }
}
