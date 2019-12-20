package com.databasepreservation.common.server.index.schema.collections;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_ACTIVITY_LOG_ACTION_COMPONENT;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_ACTIVITY_LOG_ACTION_METHOD;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_ACTIVITY_LOG_DATETIME;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_ACTIVITY_LOG_DURATION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_ACTIVITY_LOG_IP_ADDRESS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_ACTIVITY_LOG_LINE_NUMBER;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_ACTIVITY_LOG_PARAMETERS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_ACTIVITY_LOG_RELATED_OBJECT_ID;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_ACTIVITY_LOG_STATE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_ACTIVITY_LOG_USERNAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.index.schema.AbstractSolrCollection;
import com.databasepreservation.common.server.index.schema.CopyField;
import com.databasepreservation.common.server.index.schema.Field;
import com.databasepreservation.common.server.index.schema.SolrCollection;
import com.databasepreservation.common.server.index.utils.SolrUtils;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ActivityLogsCollection extends AbstractSolrCollection<ActivityLogEntry> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityLogsCollection.class);

  @Override
  public Class<ActivityLogEntry> getObjectClass() {
    return ActivityLogEntry.class;
  }

  @Override
  public String getIndexName() {
    return ViewerConstants.SOLR_INDEX_ACTIVITY_LOGS_COLLECTION_NAME;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Collections.singletonList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(SOLR_ACTIVITY_LOG_IP_ADDRESS, Field.TYPE_STRING).setIndexed(true).setRequired(true));
    fields.add(new Field(SOLR_ACTIVITY_LOG_DATETIME, Field.TYPE_DATE).setIndexed(true).setRequired(true));
    fields.add(new Field(SOLR_ACTIVITY_LOG_USERNAME, Field.TYPE_STRING).setIndexed(true).setRequired(true));
    fields.add(new Field(SOLR_ACTIVITY_LOG_ACTION_COMPONENT, Field.TYPE_STRING).setIndexed(true).setRequired(true));
    fields.add(new Field(SOLR_ACTIVITY_LOG_ACTION_METHOD, Field.TYPE_STRING).setIndexed(true).setRequired(true));
    fields.add(new Field(SOLR_ACTIVITY_LOG_STATE, Field.TYPE_STRING).setIndexed(true).setRequired(true));
    fields.add(new Field(SOLR_ACTIVITY_LOG_DURATION, Field.TYPE_LONG).setIndexed(true).setRequired(true));
    fields.add(new Field(SOLR_ACTIVITY_LOG_LINE_NUMBER, Field.TYPE_LONG).setIndexed(true).setRequired(true));
    fields.add(new Field(SOLR_ACTIVITY_LOG_PARAMETERS, Field.TYPE_STRING).setIndexed(false).setDocValues(false));

    fields.add(newIndexedStoredNotRequiredField(SOLR_ACTIVITY_LOG_RELATED_OBJECT_ID, Field.TYPE_STRING));

    return fields;
  }

  private Field newIndexedStoredNotRequiredField(String name, String type) {
    return new Field(name, type).setIndexed(true).setStored(true).setRequired(false);
  }

  private Field newStoredRequiredNotMultivaluedField(String name, String type, boolean indexed) {
    return new Field(name, type).setIndexed(indexed).setStored(true).setRequired(true).setMultiValued(false);
  }

  @Override
  public SolrInputDocument toSolrDocument(ActivityLogEntry activityLogEntry) throws ViewerException,
    RequestNotValidException, GenericException, NotFoundException, AuthorizationDeniedException {
    SolrInputDocument doc = super.toSolrDocument(activityLogEntry);

    doc.addField(SOLR_ACTIVITY_LOG_IP_ADDRESS, activityLogEntry.getAddress());
    doc.addField(SOLR_ACTIVITY_LOG_DATETIME, activityLogEntry.getDatetime());
    doc.addField(SOLR_ACTIVITY_LOG_USERNAME, activityLogEntry.getUsername());
    doc.addField(SOLR_ACTIVITY_LOG_ACTION_COMPONENT, activityLogEntry.getActionComponent());
    doc.addField(SOLR_ACTIVITY_LOG_ACTION_METHOD, activityLogEntry.getActionMethod());
    doc.addField(SOLR_ACTIVITY_LOG_STATE, activityLogEntry.getState().toString());
    doc.addField(SOLR_ACTIVITY_LOG_DURATION, activityLogEntry.getDuration());
    doc.addField(SOLR_ACTIVITY_LOG_LINE_NUMBER, activityLogEntry.getLineNumber());
    doc.addField(SOLR_ACTIVITY_LOG_PARAMETERS, JsonUtils.getJsonFromObject(activityLogEntry.getParameters()));

    return doc;
  }

  @Override
  public ActivityLogEntry fromSolrDocument(SolrDocument doc) throws ViewerException {
    ActivityLogEntry activityLogEntry = super.fromSolrDocument(doc);

    activityLogEntry.setAddress(SolrUtils.objectToString(doc.get(SOLR_ACTIVITY_LOG_IP_ADDRESS), null));
    activityLogEntry.setDatetime(SolrUtils.objectToDate(doc.get(SOLR_ACTIVITY_LOG_DATETIME)));
    activityLogEntry.setUsername(SolrUtils.objectToString(doc.get(SOLR_ACTIVITY_LOG_USERNAME), null));
    activityLogEntry.setActionComponent(SolrUtils.objectToString(doc.get(SOLR_ACTIVITY_LOG_ACTION_COMPONENT), null));
    activityLogEntry.setActionMethod(SolrUtils.objectToString(doc.get(SOLR_ACTIVITY_LOG_ACTION_METHOD), null));
    activityLogEntry.setState(
      SolrUtils.objectToEnum(doc.get(SOLR_ACTIVITY_LOG_STATE), LogEntryState.class, LogEntryState.UNKNOWN));
    activityLogEntry.setDuration(SolrUtils.objectToLong(doc.get(SOLR_ACTIVITY_LOG_DURATION), 0L));
    activityLogEntry.setLineNumber(SolrUtils.objectToLong(doc.get(SOLR_ACTIVITY_LOG_LINE_NUMBER), 0L));

    final String parameters = SolrUtils.objectToString(doc.get(SOLR_ACTIVITY_LOG_PARAMETERS), null);
    activityLogEntry.setParameters(JsonUtils.getMapFromJson(parameters == null ? "" : parameters));

    return activityLogEntry;
  }
}
