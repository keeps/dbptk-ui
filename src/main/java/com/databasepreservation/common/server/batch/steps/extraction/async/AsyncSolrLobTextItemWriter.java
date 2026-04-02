package com.databasepreservation.common.server.batch.steps.extraction.async;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.task.TaskExecutor;

import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.batch.steps.extraction.RowLobTextUpdate;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

public class AsyncSolrLobTextItemWriter implements ItemWriter<RowLobTextUpdate> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncSolrLobTextItemWriter.class);

    private final DatabaseRowsSolrManager solrManager;
    private final JobContext jobContext;
    private final TaskExecutor taskExecutor;

    public AsyncSolrLobTextItemWriter(DatabaseRowsSolrManager solrManager, JobContext jobContext, TaskExecutor taskExecutor) {
        this.solrManager = solrManager;
        this.jobContext = jobContext;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void write(Chunk<? extends RowLobTextUpdate> chunk) throws Exception {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        String databaseUUID = jobContext.getDatabaseUUID();
        List<CompletableFuture<Void>> solrFutures = new ArrayList<>();

        for (RowLobTextUpdate update : chunk.getItems()) {

            // Parallelize Solr cleanup fields
            for (String columnId : update.getColumnsToClear()) {
                solrFutures.add(CompletableFuture.runAsync(() -> {
                    try {
                        solrManager.clearExtractedLobTextField(databaseUUID, update.getUuid(), columnId);
                        updateColumnStatusState(update.getTableId(), columnId, false);
                    } catch (Exception e) {
                        LOGGER.error("Failed to clear LOB field in Solr. Row: {}, Col: {}", update.getUuid(), columnId, e);
                    }
                }, taskExecutor));
            }

            // Parallelize Solr text updates
            for (Map.Entry<String, String> entry : update.getExtractedTexts().entrySet()) {
                String columnId = entry.getKey();
                String text = entry.getValue();

                solrFutures.add(CompletableFuture.runAsync(() -> {
                    try {
                        solrManager.clearExtractedLobTextField(databaseUUID, update.getUuid(), columnId);
                        solrManager.addExtractedTextField(databaseUUID, update.getUuid(), columnId, text);
                        updateColumnStatusState(update.getTableId(), columnId, true);
                    } catch (Exception e) {
                        LOGGER.error("Failed to persist extracted text in Solr. Row: {}, Col: {}", update.getUuid(), columnId, e);
                    }
                }, taskExecutor));
            }
        }

        // Wait for all Solr HTTP requests to finish before completing the chunk write phase
        CompletableFuture.allOf(solrFutures.toArray(new CompletableFuture[0])).join();
    }

    private void updateColumnStatusState(String tableId, String columnId, boolean isExtracted) {
        synchronized (jobContext) {
            ColumnStatus columnStatus = jobContext.getCollectionStatus().getColumnByTableIdAndColumn(tableId, columnId);
            if (columnStatus != null && columnStatus.getLobTextExtractionStatus() != null) {
                columnStatus.getLobTextExtractionStatus().setExtractedAndIndexedText(isExtracted);
            }
        }
    }
}