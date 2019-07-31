/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package com.databasepreservation.main.common.server.index.schema.collections;

import static com.databasepreservation.main.common.shared.ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX;
import static com.databasepreservation.main.common.shared.ViewerConstants.SOLR_ROWS_TABLE_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.DateTime;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.main.common.server.index.factory.SolrClientFactory;
import com.databasepreservation.main.common.server.index.schema.AbstractSolrCollection;
import com.databasepreservation.main.common.server.index.schema.CopyField;
import com.databasepreservation.main.common.server.index.schema.Field;
import com.databasepreservation.main.common.server.index.schema.SolrBootstrapUtils;
import com.databasepreservation.main.common.server.index.schema.SolrCollection;
import com.databasepreservation.main.common.server.index.schema.SolrRowsCollectionRegistry;
import com.databasepreservation.main.common.server.index.utils.SolrUtils;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerCell;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerRow;
import com.databasepreservation.main.common.shared.exceptions.ViewerException;
import com.databasepreservation.utils.JodaUtils;

public class RowsCollection extends AbstractSolrCollection<ViewerRow> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RowsCollection.class);

  private String databaseUUID;

  public RowsCollection(String databaseUUID) {
    this.databaseUUID = databaseUUID;
  }

  public String getDatabaseUUID() {
    return this.databaseUUID;
  }

  @Override
  public Class<ViewerRow> getObjectClass() {
    return ViewerRow.class;
  }

  @Override
  public String getIndexName() {
    return SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + databaseUUID;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Collections.singletonList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(SOLR_ROWS_TABLE_ID, Field.TYPE_STRING).setIndexed(true).setStored(true));

    return fields;
  }

  private Field newIndexedStoredNotRequiredField(String name, String type) {
    return new Field(name, type).setIndexed(true).setStored(true).setRequired(false);
  }

  @Override
  public SolrInputDocument toSolrDocument(ViewerRow row) throws ViewerException, RequestNotValidException,
      GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(row);

    doc.setField(ViewerConstants.SOLR_ROWS_TABLE_ID, row.getTableId());
    for (Map.Entry<String, ViewerCell> cellEntry : row.getCells().entrySet()) {
      String solrColumnName = cellEntry.getKey();
      String cellValue = cellEntry.getValue().getValue();

      doc.addField(solrColumnName, cellValue);
    }

    return doc;
  }

  @Override
  public ViewerRow fromSolrDocument(SolrDocument doc) throws ViewerException {
    ViewerRow viewerRow = super.fromSolrDocument(doc);

    viewerRow.setTableId(SolrUtils.objectToString(doc.get(ViewerConstants.SOLR_ROWS_TABLE_ID), null));

    Map<String, ViewerCell> cells = new HashMap<>();
    for (Map.Entry<String, Object> entry : doc) {
      String columnName = entry.getKey();
      Object value = entry.getValue();
      cellFromEntry(columnName, value).ifPresent(viewerCell -> cells.put(columnName, viewerCell));
    }
    viewerRow.setCells(cells);

    return viewerRow;
  }

  private Optional<ViewerCell> cellFromEntry(String columnName, Object value) {
    Optional<ViewerCell> viewerCell = Optional.empty();

    if (columnName.startsWith(ViewerConstants.SOLR_INDEX_ROW_COLUMN_NAME_PREFIX)) {
      if (value instanceof Date) {
        DateTime date = new DateTime(value, JodaUtils.DEFAULT_CHRONOLOGY);
        if (columnName.endsWith(ViewerConstants.SOLR_DYN_TDATE)) {
          viewerCell = Optional.of(new ViewerCell(JodaUtils.solrDateDisplay(date)));
        } else if (columnName.endsWith(ViewerConstants.SOLR_DYN_TTIME)) {
          viewerCell = Optional.of(new ViewerCell(JodaUtils.solrTimeDisplay(date)));
        } else if (columnName.endsWith(ViewerConstants.SOLR_DYN_TDATETIME)) {
          viewerCell = Optional.of(new ViewerCell(JodaUtils.solrDateTimeDisplay(date)));
        } else {
          viewerCell = Optional.of(new ViewerCell(value.toString()));
        }
      } else {
        viewerCell = Optional.of(new ViewerCell(value.toString()));
      }
    } else if (columnName.startsWith(ViewerConstants.SOLR_INDEX_ROW_LOB_COLUMN_NAME_PREFIX)) {
      viewerCell = Optional.of(new ViewerCell(value.toString()));
    }

    return viewerCell;
  }

  public void createRowsCollection() {
    LOGGER.info("Creating SOLR collection {}", getIndexName());
    if (SolrClientFactory.get().createCollection(getIndexName())) {
      try {
        SolrBootstrapUtils.bootstrapRowsCollection(SolrClientFactory.get().getSolrClient(), this);
      } catch (ViewerException e) {
        LOGGER.error("Could not create collection " + getIndexName(), e);
      }
      SolrRowsCollectionRegistry.register(this);
    } else {
      LOGGER.error("Could not create collection {}", getIndexName());
    }

  }
}
