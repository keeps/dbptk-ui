/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package com.databasepreservation.common.server.index.schema.collections;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_BROWSE_LOAD_DATE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_METADATA;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_SIARD_PATH;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_SIARD_SIZE;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_SIARD_VERSION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_STATUS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATED_AT;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATE_VERSION;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATION_ERRORS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATION_PASSED;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATION_SKIPPED;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATION_STATUS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATION_WARNINGS;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_DATABASES_VALIDATOR_REPORT_PATH;

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
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.index.schema.AbstractSolrCollection;
import com.databasepreservation.common.server.index.schema.CopyField;
import com.databasepreservation.common.server.index.schema.Field;
import com.databasepreservation.common.server.index.schema.SolrCollection;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.server.index.utils.SolrUtils;

public class DatabasesCollection extends AbstractSolrCollection<ViewerDatabase> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabasesCollection.class);

  @Override
  public Class<ViewerDatabase> getObjectClass() {
    return ViewerDatabase.class;
  }

  @Override
  public String getIndexName() {
    return ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME;
  }

  @Override
  public List<CopyField> getCopyFields() {
    return Collections.singletonList(SolrCollection.getCopyAllToSearchField());
  }

  @Override
  public List<Field> getFields() {
    List<Field> fields = new ArrayList<>(super.getFields());

    fields.add(new Field(SOLR_DATABASES_STATUS, Field.TYPE_STRING).setIndexed(true).setRequired(true));
    fields.add(new Field(SOLR_DATABASES_METADATA, Field.TYPE_STRING).setIndexed(false).setStored(true)
      .setRequired(false).setDocValues(false));
    fields.add(new Field(SOLR_DATABASES_VALIDATION_STATUS, Field.TYPE_STRING).setIndexed(true).setRequired(true));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_SIARD_PATH, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_SIARD_SIZE, Field.TYPE_LONG));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_SIARD_VERSION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATED_AT, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATE_VERSION, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATOR_REPORT_PATH, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATION_PASSED, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATION_ERRORS, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATION_WARNINGS, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_VALIDATION_SKIPPED, Field.TYPE_STRING));
    fields.add(newIndexedStoredNotRequiredField(SOLR_DATABASES_BROWSE_LOAD_DATE, Field.TYPE_STRING));

    return fields;
  }

  private Field newIndexedStoredNotRequiredField(String name, String type) {
    return new Field(name, type).setIndexed(true).setStored(true).setRequired(false);
  }

  @Override
  public SolrInputDocument toSolrDocument(ViewerDatabase object) throws ViewerException, RequestNotValidException,
    GenericException, NotFoundException, AuthorizationDeniedException {

    SolrInputDocument doc = super.toSolrDocument(object);

    doc.addField(SOLR_DATABASES_STATUS, object.getStatus().toString());
    doc.addField(SOLR_DATABASES_METADATA, JsonTransformer.getJsonFromObject(object.getMetadata()));

    doc.addField(SOLR_DATABASES_SIARD_PATH, object.getPath());
    doc.addField(SOLR_DATABASES_SIARD_SIZE, object.getSize());
    doc.addField(SOLR_DATABASES_SIARD_VERSION, object.getVersion());

    doc.addField(SOLR_DATABASES_VALIDATED_AT, object.getValidatedAt());
    doc.addField(SOLR_DATABASES_VALIDATOR_REPORT_PATH, object.getValidatorReportPath());
    doc.addField(SOLR_DATABASES_VALIDATE_VERSION, object.getValidatedVersion());
    doc.addField(SOLR_DATABASES_VALIDATION_STATUS, object.getValidationStatus().toString());
    doc.addField(SOLR_DATABASES_VALIDATION_PASSED, object.getValidationPassed());
    doc.addField(SOLR_DATABASES_VALIDATION_ERRORS, object.getValidationWarnings());
    doc.addField(SOLR_DATABASES_VALIDATION_WARNINGS, object.getValidationWarnings());
    doc.addField(SOLR_DATABASES_VALIDATION_SKIPPED, object.getValidationSkipped());
    doc.addField(SOLR_DATABASES_BROWSE_LOAD_DATE, object.getLoadedAt());

    return doc;
  }

  @Override
  public ViewerDatabase fromSolrDocument(SolrDocument doc) throws ViewerException {
    ViewerDatabase viewerDatabase = super.fromSolrDocument(doc);

    viewerDatabase.setStatus(SolrUtils.objectToEnum(doc.get(SOLR_DATABASES_STATUS), ViewerDatabaseStatus.class,
        ViewerDatabaseStatus.INGESTING));

    String jsonMetadata = SolrUtils.objectToString(doc.get(SOLR_DATABASES_METADATA), "");
    ViewerMetadata metadata = JsonTransformer.getObjectFromJson(jsonMetadata, ViewerMetadata.class);
    viewerDatabase.setMetadata(metadata);

    viewerDatabase.setPath(SolrUtils.objectToString(doc.get(SOLR_DATABASES_SIARD_PATH), ""));
    viewerDatabase.setSize(SolrUtils.objectToLong(doc.get(SOLR_DATABASES_SIARD_SIZE), 0L));
    viewerDatabase.setVersion(SolrUtils.objectToString(doc.get(SOLR_DATABASES_SIARD_VERSION), "2.1"));

    viewerDatabase.setValidatedAt(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATED_AT), ""));
    viewerDatabase.setValidatorReportPath(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATOR_REPORT_PATH), ""));
    viewerDatabase.setValidatedVersion(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATE_VERSION), ""));
    viewerDatabase.setValidationStatus(SolrUtils.objectToEnum(doc.get(SOLR_DATABASES_VALIDATION_STATUS),
        ViewerDatabaseValidationStatus.class, ViewerDatabaseValidationStatus.NOT_VALIDATED));
    viewerDatabase.setValidationPassed(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATION_PASSED), ""));
    viewerDatabase.setValidationErrors(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATION_ERRORS), ""));
    viewerDatabase.setValidationWarnings(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATION_WARNINGS), ""));
    viewerDatabase.setValidationSkipped(SolrUtils.objectToString(doc.get(SOLR_DATABASES_VALIDATION_SKIPPED), ""));
    viewerDatabase.setLoadedAt(SolrUtils.objectToString(doc.get(SOLR_DATABASES_BROWSE_LOAD_DATE), ""));

    return viewerDatabase;
  }
}
