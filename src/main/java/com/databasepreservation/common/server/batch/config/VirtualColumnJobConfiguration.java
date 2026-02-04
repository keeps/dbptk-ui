package com.databasepreservation.common.server.batch.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.roda.core.data.exceptions.GenericException;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.steps.common.readers.SolrCursorItemReader;
import com.databasepreservation.common.server.batch.steps.common.listners.JobListener;
import com.databasepreservation.common.server.batch.steps.common.listners.ProgressChunkListener;
import com.databasepreservation.common.server.index.DatabaseRowsSolrManager;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
//@Configuration
public class VirtualColumnJobConfiguration {

  private final DatabaseRowsSolrManager solrManager;

  public VirtualColumnJobConfiguration(DatabaseRowsSolrManager solrManager) {
    this.solrManager = solrManager;
  }

  @Autowired
  private JobListener jobListener;

  @Bean
  @StepScope
  public ProgressChunkListener progressListener() {
    return new ProgressChunkListener(solrManager);
  }

  @Bean
  @StepScope
  public CollectionStatus sharedCollectionStatus(
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID) {

    try {
      return ViewerFactory.getConfigurationManager().getConfigurationCollection(databaseUUID, databaseUUID);
    } catch (GenericException e) {
      throw new org.springframework.beans.factory.BeanCreationException("Unable to load CollectionStatus", e);
    }
  }

  @Bean
  @StepScope
  public SolrCursorItemReader columnReader(
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_TABLE_ID_PARAM + "']}") String tableUUID,
    CollectionStatus status) {

    TableStatus tableStatus = status.getTableStatus(tableUUID);
    List<String> fieldsToReturn = tableStatus.getColumns().stream()
      .filter(column -> column.getType().equals(ViewerType.dbTypes.VIRTUAL))
      .flatMap(column -> column.getVirtualColumnStatus().getSourceColumnsIds().stream()).collect(Collectors.toList());
    fieldsToReturn.add(ViewerConstants.INDEX_ID);

    Filter filter = FilterUtils.filterByTable(new Filter(), tableStatus.getId());
    return new SolrCursorItemReader(solrManager, databaseUUID, filter, fieldsToReturn);
  }

  @Bean
  @StepScope
  public ItemProcessor<ViewerRow, VirtualColumnWrapper> virtualColumnProcessor(
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID,
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_TABLE_ID_PARAM + "']}") String tableUUID,
    CollectionStatus status) {

    TableStatus tableStatus = status.getTableStatus(tableUUID);

    List<ColumnStatus> virtualColumns = tableStatus.getColumns().stream()
      .filter(column -> column.getType() == ViewerType.dbTypes.VIRTUAL).toList();

    return new ItemProcessor<ViewerRow, VirtualColumnWrapper>() {
      @Override
      public VirtualColumnWrapper process(ViewerRow row) throws Exception {
        VirtualColumnWrapper virtualColumnWrapper = new VirtualColumnWrapper(row);

        for (ColumnStatus virtualColumn : virtualColumns) {
          ViewerCell viewerCell = new ViewerCell();
          VirtualColumnStatus virtualColumnStatus = virtualColumn.getVirtualColumnStatus();

          // TODO: Merge sourceCells values into a single value using handlebars template
          TemplateStatus sourceTemplateStatus = virtualColumnStatus.getSourceTemplateStatus();
          viewerCell.setValue(sourceTemplateStatus.getTemplate());
          VirtualColumnWrapper.Cells cells = new VirtualColumnWrapper.Cells(virtualColumn.getName(), viewerCell);
          virtualColumnWrapper.getCells().add(cells);
        }

        return virtualColumnWrapper;
      }
    };
  }

  @Bean
  @StepScope
  public ItemWriter<List<VirtualColumnWrapper>> virtualColumnWriter(
    @Value("#{jobParameters['" + ViewerConstants.CONTROLLER_DATABASE_ID_PARAM + "']}") String databaseUUID) {
    return new ItemWriter<List<VirtualColumnWrapper>>() {
      @Override
      public void write(Chunk<? extends List<VirtualColumnWrapper>> chunk) throws Exception {
        for (List<VirtualColumnWrapper> virtualColumnWrappers : chunk) {
          solrManager.addVirtualCell(databaseUUID, virtualColumnWrappers);
        }
      }
    };
  }

  public static class VirtualColumnWrapper {
    private final ViewerRow row;
    private List<Cells> cells;

    public static class Cells {
        private final String solrName;
        private final ViewerCell cell;

        public Cells(String solrName, ViewerCell cell) {
          this.solrName = solrName;
          this.cell = cell;
        }

        public String getSolrName() {
          return solrName;
        }

        public ViewerCell getCell() {
          return cell;
        }
    }

    public VirtualColumnWrapper(ViewerRow row) {
      this.row = row;
      this.cells = new ArrayList<>();
    }

    public ViewerRow getRow() {
      return row;
    }

    public List<Cells> getCells() {
      return cells;
    }

  }
}
