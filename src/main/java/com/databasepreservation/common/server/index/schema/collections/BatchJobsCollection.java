package com.databasepreservation.common.server.index.schema.collections;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_CREATE_TIME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_DATABASE_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_DATABASE_UUID;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_END_TIME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_EXIT_CODE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_EXIT_DESCRIPTION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_ID;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_ROWS_PROCESSED;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_ROWS_TO_PROCESS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_SCHEMA_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_START_TIME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_STATUS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_TABLE_NAME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_BATCH_JOB_TABLE_UUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.index.schema.AbstractSolrCollection;
import com.databasepreservation.common.server.index.schema.CopyField;
import com.databasepreservation.common.server.index.schema.Field;
import com.databasepreservation.common.server.index.schema.SolrCollection;
import com.databasepreservation.common.server.index.utils.SolrUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BatchJobsCollection extends AbstractSolrCollection<ViewerJob> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BatchJobsCollection.class);

  @Override
  public Class<ViewerJob> getObjectClass() {
    return ViewerJob.class;
  }

  @Override
  public String getIndexName() {
    return ViewerConstants.SOLR_INDEX_BATCH_JOBS_COLLECTION_NAME;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Collections.singletonList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(SOLR_BATCH_JOB_ID, Field.TYPE_LONG).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_DATABASE_UUID, Field.TYPE_STRING).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_DATABASE_NAME, Field.TYPE_STRING).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_TABLE_UUID, Field.TYPE_STRING).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_SCHEMA_NAME, Field.TYPE_STRING).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_TABLE_NAME, Field.TYPE_STRING).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_NAME, Field.TYPE_STRING).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_STATUS, Field.TYPE_STRING).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_CREATE_TIME, Field.TYPE_DATE).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_START_TIME, Field.TYPE_DATE).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_END_TIME, Field.TYPE_DATE).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_EXIT_CODE, Field.TYPE_STRING).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_EXIT_DESCRIPTION, Field.TYPE_STRING).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_ROWS_TO_PROCESS, Field.TYPE_LONG).setIndexed(true).setRequired(false));
    fields.add(new Field(SOLR_BATCH_JOB_ROWS_PROCESSED, Field.TYPE_LONG).setIndexed(true).setRequired(false));

    return fields;
  }

  @Override
  public SolrInputDocument toSolrDocument(ViewerJob viewerJob) throws ViewerException, RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException {
    SolrInputDocument doc = super.toSolrDocument(viewerJob);

    doc.addField(SOLR_BATCH_JOB_ID, viewerJob.getJobId());
    doc.addField(SOLR_BATCH_JOB_DATABASE_UUID, viewerJob.getDatabaseUuid());
    doc.addField(SOLR_BATCH_JOB_DATABASE_NAME, viewerJob.getDatabaseName());
    doc.addField(SOLR_BATCH_JOB_TABLE_UUID, viewerJob.getTableUuid());
    doc.addField(SOLR_BATCH_JOB_SCHEMA_NAME, viewerJob.getSchemaName());
    doc.addField(SOLR_BATCH_JOB_TABLE_NAME, viewerJob.getTableName());
    doc.addField(SOLR_BATCH_JOB_NAME, viewerJob.getName());
    doc.addField(SOLR_BATCH_JOB_STATUS, viewerJob.getStatus().toString());
    doc.addField(SOLR_BATCH_JOB_CREATE_TIME, viewerJob.getCreateTime());
    doc.addField(SOLR_BATCH_JOB_START_TIME, viewerJob.getStartTime());
    doc.addField(SOLR_BATCH_JOB_END_TIME, viewerJob.getEndTime());
    doc.addField(SOLR_BATCH_JOB_EXIT_CODE, viewerJob.getExitCode());
    doc.addField(SOLR_BATCH_JOB_EXIT_DESCRIPTION, viewerJob.getExitDescription());
    doc.addField(SOLR_BATCH_JOB_ROWS_TO_PROCESS, viewerJob.getRowsToProcess());
    doc.addField(SOLR_BATCH_JOB_ROWS_PROCESSED, viewerJob.getProcessRows());

    return doc;
  }

  @Override
  public ViewerJob fromSolrDocument(SolrDocument doc) throws ViewerException {
    ViewerJob viewerJob = super.fromSolrDocument(doc);

    viewerJob.setJobId(SolrUtils.objectToLong(doc.get(SOLR_BATCH_JOB_ID), null));
    viewerJob.setDatabaseUuid(SolrUtils.objectToString(doc.get(SOLR_BATCH_JOB_DATABASE_UUID), null));
    viewerJob.setDatabaseName(SolrUtils.objectToString(doc.get(SOLR_BATCH_JOB_DATABASE_NAME), null));
    viewerJob.setTableUuid(SolrUtils.objectToString(doc.get(SOLR_BATCH_JOB_TABLE_UUID), null));
    viewerJob.setSchemaName(SolrUtils.objectToString(doc.get(SOLR_BATCH_JOB_SCHEMA_NAME), null));
    viewerJob.setTableName(SolrUtils.objectToString(doc.get(SOLR_BATCH_JOB_TABLE_NAME), null));
    viewerJob.setName(SolrUtils.objectToString(doc.get(SOLR_BATCH_JOB_NAME), null));
    viewerJob.setStatus(
      SolrUtils.objectToEnum(doc.get(SOLR_BATCH_JOB_STATUS), ViewerJobStatus.class, ViewerJobStatus.UNKNOWN));
    viewerJob.setCreateTime(SolrUtils.objectToDate(doc.get(SOLR_BATCH_JOB_CREATE_TIME)));
    viewerJob.setStartTime(SolrUtils.objectToDate(doc.get(SOLR_BATCH_JOB_START_TIME)));
    viewerJob.setEndTime(SolrUtils.objectToDate(doc.get(SOLR_BATCH_JOB_END_TIME)));
    viewerJob.setExitCode(SolrUtils.objectToString(doc.get(SOLR_BATCH_JOB_EXIT_CODE), null));
    viewerJob.setExitDescription(SolrUtils.objectToString(doc.get(SOLR_BATCH_JOB_EXIT_DESCRIPTION), null));
    viewerJob.setRowsToProcess(SolrUtils.objectToLong(doc.get(SOLR_BATCH_JOB_ROWS_TO_PROCESS), null));
    viewerJob.setProcessRows(SolrUtils.objectToLong(doc.get(SOLR_BATCH_JOB_ROWS_PROCESSED), null));

    return viewerJob;
  }
}
