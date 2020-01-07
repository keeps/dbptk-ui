package com.databasepreservation.common.server.index;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.models.structure.ViewerJob;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.exceptions.SavedSearchException;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.configuration.collection.CollectionConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseFromToolkit;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.index.schema.SolrCollection;
import com.databasepreservation.common.server.index.schema.SolrDefaultCollectionRegistry;
import com.databasepreservation.common.server.index.schema.SolrRowsCollectionRegistry;
import com.databasepreservation.common.server.index.schema.collections.RowsCollection;
import com.databasepreservation.common.server.index.utils.IterableIndexResult;
import com.databasepreservation.common.server.index.utils.IterableNestedIndexResult;
import com.databasepreservation.common.server.index.utils.JsonTransformer;
import com.databasepreservation.common.server.index.utils.Pair;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.utils.FileUtils;

/**
 * Exposes some methods to interact with a Solr Server
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabaseRowsSolrManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseRowsSolrManager.class);
  private static final long INSERT_DOCUMENT_TIMEOUT = 60000; // 60 seconds

  private final SolrClient client;

  public DatabaseRowsSolrManager(SolrClient client) {
    this.client = client;
  }

  public String findSIARDFile(String path) throws GenericException, RequestNotValidException {
    Filter pathFilter = new Filter(new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_SIARD_PATH, path));
    return SolrUtils.findSIARDFile(client, ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME, pathFilter);
  }

  /**
   * Adds a database to the databases collection
   * 
   * @param database
   *          the new database
   */
  public void addDatabaseMetadata(ViewerDatabase database) throws ViewerException {
    // add this database to the collection
    insertDocument(ViewerDatabase.class, database);
    // Delegate
    try {
      ViewerFactory.getConfigurationManager().addDatabase(database);
    } catch (GenericException e) {
      throw new ViewerException(e);
    }
  }

  public void addDatabaseRowCollection(final String databaseUUID) throws ViewerException {
    updateValidationFields(databaseUUID,
      Pair.of(ViewerConstants.SOLR_DATABASES_STATUS, ViewerDatabaseStatus.INGESTING.toString()));
    RowsCollection collection = new RowsCollection(databaseUUID);
    collection.createRowsCollection();
    // Delegate
    ViewerFactory.getConfigurationManager().addCollection(databaseUUID, collection.getIndexName());
  }

  public void removeDatabase(ViewerDatabase database, Path lobFolder) throws ViewerException {
    // delete the LOBs
    if (lobFolder != null) {
      try {
        FileUtils.deleteDirectoryRecursiveQuietly(lobFolder.resolve(database.getUuid()));
      } catch (InvalidPathException e) {
        throw new ViewerException("Error deleting LOBs for database " + database.getUuid(), e);
      }
    }

    // delete related rows collection
    String rowsCollectionName = SolrRowsCollectionRegistry.get(database.getUuid()).getIndexName();
    SolrRequest<?> request = CollectionAdminRequest.deleteCollection(rowsCollectionName);
    try {
      client.request(request);
      LOGGER.debug("Deleted collection {}", rowsCollectionName);
    } catch (SolrServerException | IOException | SolrException e) {
      throw new ViewerException("Error deleting collection " + rowsCollectionName, e);
    }

    // delete related saved searches
    Filter savedSearchFilter = new Filter(
      new SimpleFilterParameter(ViewerConstants.SOLR_SEARCHES_DATABASE_UUID, database.getUuid()));
    try {
      SolrUtils.delete(client, SolrDefaultCollectionRegistry.get(SavedSearch.class), savedSearchFilter);
      LOGGER.debug("Deleted saved searches for database {}", database.getUuid());
    } catch (GenericException | RequestNotValidException e) {
      throw new ViewerException("Error deleting saved searches for database " + database.getUuid(), e);
    }

    // delete the database item
    try {
      SolrUtils.delete(client, SolrDefaultCollectionRegistry.get(ViewerDatabase.class),
        Collections.singletonList(database.getUuid()));
      LOGGER.debug("Deleted database {}", database.getUuid());
    } catch (GenericException e) {
      throw new ViewerException("Error deleting the database " + database.getUuid(), e);
    }
  }

  /**
   * Does nothing. Just a part of the database traversal
   *
   * @param table
   *          the table
   */
  public void addTable(String databaseUUID, ViewerTable table) throws ViewerException {
    ViewerFactory.getConfigurationManager().addTable(databaseUUID, table);
  }

  public void addRow(String databaseUUID, ViewerRow row) throws ViewerException {
    RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);

    try {
      insertDocument(collection.getIndexName(), collection.toSolrDocument(row));
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      throw new ViewerException(e);
    }
  }

  public void addRow(ViewerDatabaseFromToolkit viewerDatabase, ViewerRow row) throws ViewerException {
    RowsCollection collection = SolrRowsCollectionRegistry.get(viewerDatabase.getUuid());

    try {
      insertDocument(collection.getIndexName(), collection.toSolrDocument(row));
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      throw new ViewerException(e);
    }
  }

  public <T extends IsIndexed> IndexResult<T> find(Class<T> classToReturn, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, List<String> fieldsToReturn) throws GenericException, RequestNotValidException {
    return SolrUtils.find(client, SolrDefaultCollectionRegistry.get(classToReturn), filter, sorter, sublist, facets,
      fieldsToReturn);
  }

  public <T extends IsIndexed> IndexResult<T> find(Class<T> classToReturn, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets) throws GenericException, RequestNotValidException {
    return find(classToReturn, filter, sorter, sublist, facets, new ArrayList<>());
  }

  public <T extends IsIndexed> Long count(Class<T> classToReturn, Filter filter)
    throws GenericException, RequestNotValidException {
    return SolrUtils.count(client, SolrDefaultCollectionRegistry.get(classToReturn), filter);
  }

  public <T extends IsIndexed> T retrieve(Class<T> classToReturn, String id)
    throws NotFoundException, GenericException {
    return SolrUtils.retrieve(client, SolrDefaultCollectionRegistry.get(classToReturn), id);
  }

  public IndexResult<ViewerRow> findRows(String databaseUUID, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets) throws GenericException, RequestNotValidException {
    return SolrUtils.findRows(client, databaseUUID, filter, sorter, sublist, facets);
  }

  public IndexResult<ViewerRow> findRows(String databaseUUID, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, List<String> fieldsToReturn) throws GenericException, RequestNotValidException {
    return SolrUtils.findRows(client, databaseUUID, filter, sorter, sublist, facets, fieldsToReturn);
  }

  public IterableIndexResult findAllRows(String databaseUUID, final Filter filter, final Sorter sorter,
    final List<String> fieldsToReturn) {
    return new IterableIndexResult(client, databaseUUID, filter, sorter, fieldsToReturn);
  }

  public IterableNestedIndexResult findAllRows(String databaseUUID, SolrQuery query, final Sorter sorter) {
    return new IterableNestedIndexResult(client, databaseUUID, query, sorter);
  }

  public IndexResult<ViewerRow> findRows(String databaseUUID, List<SolrQuery> queryList)
    throws GenericException, RequestNotValidException {
    return SolrUtils.findRowsWithSubQuery(client, databaseUUID, queryList);
  }

  public <T extends IsIndexed> Long countRows(String databaseUUID, Filter filter)
    throws GenericException, RequestNotValidException {
    return SolrUtils.countRows(client, databaseUUID, filter);
  }

  public ViewerRow retrieveRows(String databaseUUID, String rowUUID) throws NotFoundException, GenericException {
    return SolrUtils.retrieveRows(client, databaseUUID, rowUUID);
  }

  public void addLogEntry(ActivityLogEntry logEntry) throws NotFoundException, GenericException {
    SolrCollection<ActivityLogEntry> activityLogEntrySolrCollection = SolrDefaultCollectionRegistry
      .get(ActivityLogEntry.class);
    try {
      SolrInputDocument doc = activityLogEntrySolrCollection.toSolrDocument(logEntry);
      client.add(activityLogEntrySolrCollection.getIndexName(), doc);
      client.commit(activityLogEntrySolrCollection.getIndexName(), true, true, true);
    } catch (ViewerException | AuthorizationDeniedException | RequestNotValidException e) {
      LOGGER.debug("Solr error while converting to document", e);
    } catch (IOException e) {
      LOGGER.debug("IOException while attempting to save activity log entry", e);
    } catch (SolrServerException e) {
      LOGGER.debug("Solr error while attempting to save activity log entry", e);
    }
  }

  public void addBatchJob(ViewerJob batchJob) throws NotFoundException, GenericException {
    SolrCollection<ViewerJob> viewerJobSolrCollection = SolrDefaultCollectionRegistry.get(ViewerJob.class);
    try {
      SolrInputDocument doc = viewerJobSolrCollection.toSolrDocument(batchJob);
      client.add(viewerJobSolrCollection.getIndexName(), doc);
      client.commit(viewerJobSolrCollection.getIndexName(), true, true, true);
    } catch (ViewerException | AuthorizationDeniedException | RequestNotValidException e) {
      LOGGER.debug("Solr error while converting to document", e);
    } catch (SolrServerException e) {
      LOGGER.debug("Solr error while attempting to save batch job", e);
    } catch (IOException e) {
      LOGGER.debug("IOException while attempting to save batch job", e);
    }
  }

  public void addSavedSearch(SavedSearch savedSearch) throws NotFoundException, GenericException {
    SolrCollection<SavedSearch> savedSearchesCollection = SolrDefaultCollectionRegistry.get(SavedSearch.class);

    try {
      SolrInputDocument doc = savedSearchesCollection.toSolrDocument(savedSearch);
      client.add(savedSearchesCollection.getIndexName(), doc);
      client.commit(savedSearchesCollection.getIndexName(), true, true, true);
      // Delegate
      ViewerFactory.getConfigurationManager().addSearch(savedSearch);
    } catch (ViewerException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.debug("Solr error while converting to document", e);
    } catch (SolrServerException e) {
      LOGGER.debug("Solr error while attempting to save search", e);
    } catch (IOException e) {
      LOGGER.debug("IOException while attempting to save search", e);
    }
  }

  public void editSavedSearch(String databaseUUID, String uuid, String name, String description) throws SavedSearchException {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(ViewerConstants.INDEX_ID, uuid);
    doc.addField(ViewerConstants.SOLR_SEARCHES_NAME, SolrUtils.asValueUpdate(name));
    doc.addField(ViewerConstants.SOLR_SEARCHES_DESCRIPTION, SolrUtils.asValueUpdate(description));
    try {
      client.add(ViewerConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, doc);
      client.commit(ViewerConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, true, true);
      // Delegate
      ViewerFactory.getConfigurationManager().editSearch(databaseUUID, uuid, name, description);
    } catch (SolrException | SolrServerException e) {
      throw new SavedSearchException("Solr error while attempting to save search", e);
    } catch (IOException e) {
      throw new SavedSearchException("IOException while attempting to save search", e);
    }
  }

  public void deleteSavedSearch(String uuid) throws SavedSearchException {
    try {
      client.deleteById(ViewerConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, uuid);
      client.commit(ViewerConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, true, true);
    } catch (SolrServerException e) {
      throw new SavedSearchException("Solr error while attempting to save search", e);
    } catch (IOException e) {
      throw new SavedSearchException("IOException while attempting to save search", e);
    }
  }

  public void deleteDatabasesCollection(final String UUID) {
    try {
      client.deleteById(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME, UUID);
      client.commit(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME, true, true);
    } catch (SolrServerException e) {
      LOGGER.debug("Solr error while attempting to delete search", e);
    } catch (IOException e) {
      LOGGER.debug("IOException while attempting to delete search", e);
    }
  }

  private <T extends IsIndexed> void insertDocument(Class<T> objClass, T obj) throws ViewerException {
    SolrCollection<T> solrCollection = SolrDefaultCollectionRegistry.get(objClass);
    try {
      insertDocument(solrCollection.getIndexName(), solrCollection.toSolrDocument(obj));
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      LOGGER.error("Could not insert document '{}' of class '{}' in index '{}'", obj, objClass.getName(),
        solrCollection.getIndexName());
    }
  }

  /**
   * The collections are not immediately available after creation, this method
   * makes sequential attempts to insert a document before giving up (by timeout)
   *
   * @throws ViewerException
   *           in case of a fatal error
   */
  private void insertDocument(String collection, SolrInputDocument doc) throws ViewerException {
    long timeoutStart = System.currentTimeMillis();
    int tries = 0;
    boolean insertedAllDocuments = false;
    do {
      UpdateResponse response = null;
      try {
        response = client.add(collection, doc, 1000);
        if (response.getStatus() == 0) {
          insertedAllDocuments = true;
          break;
        } else {
          LOGGER.warn(
            "Could not insert a document batch in collection " + collection + ". Response: " + response.toString());
        }
      } catch (SolrException e) {
        if (e.code() == 404) {
          // this means that the collection does not exist yet. retry
          LOGGER.debug("Collection " + collection + " does not exist (yet). Retrying (" + tries + ")");
        } else {
          LOGGER.warn(
            "Could not insert a document batch in collection" + collection + ". Last response (if any): " + response,
            e);
        }
      } catch (SolrServerException | IOException e) {
        throw new ViewerException("Problem adding information", e);
      }

      if (!insertedAllDocuments) {
        // wait a moment and then retry (or reach timeout and fail)
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          LOGGER.debug("insertDocument sleep was interrupted", e);
        }
        tries++;
      }
    } while (System.currentTimeMillis() - timeoutStart < INSERT_DOCUMENT_TIMEOUT && !insertedAllDocuments);

    if (!insertedAllDocuments) {
      throw new ViewerException(
        "Could not insert a document batch in collection. Reason: Timeout reached while waiting for collection to be available.");
    }
  }

  public void markDatabaseCollection(final String databaseUUID, ViewerDatabaseStatus status) {
    updateDatabaseFields(databaseUUID, Pair.of(ViewerConstants.SOLR_DATABASES_STATUS, status.toString()));
  }

  public void markDatabaseAsReady(final String databaseUUID) throws ViewerException {
    updateDatabaseFields(databaseUUID,
      Pair.of(ViewerConstants.SOLR_DATABASES_STATUS, ViewerDatabaseStatus.AVAILABLE.toString()));
  }

  @SafeVarargs
  private final void updateDatabaseFields(String databaseUUID, Pair<String, ?>... fields) {
    // create document to update this DB
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, databaseUUID);

    // add all the fields that will be updated
    for (Pair<String, ?> field : fields) {
      LOGGER.debug("Updating " + field.getFirst() + " to " + field.getSecond());
      doc.addField(field.getFirst(), SolrUtils.asValueUpdate(field.getSecond()));
    }

    // send it to Solr
    try {
      insertDocument(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME, doc);
    } catch (ViewerException e) {
      LOGGER.error("Could not update database progress for {}", databaseUUID, e);
    }
  }

  public void updateSIARDValidationInformation(String databaseUUID, ViewerDatabaseValidationStatus validationStatus,
    String validatorReportLocation, String DBPTKVersion, String validationDate) {

    updateValidationFields(databaseUUID, Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATED_AT, validationDate),
      Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATOR_REPORT_PATH, validatorReportLocation),
      Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATE_VERSION, DBPTKVersion),
      Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATION_STATUS, validationStatus.toString()));
    // delegate
    ViewerFactory.getConfigurationManager().updateValidationStatus(databaseUUID, validationStatus, validationDate,
      validatorReportLocation, DBPTKVersion);
  }

  public void updateSIARDValidationIndicators(String databaseUUID, String passed, String ok, String failed,
    String errors, String warnings, String skipped) {
    updateValidationFields(databaseUUID, Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATION_PASSED, passed),
      Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATION_OK, ok),
      Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATION_FAILED, failed),
      Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATION_ERRORS, errors),
      Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATION_WARNINGS, warnings),
      Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATION_SKIPPED, skipped));
    // delegate
    ViewerFactory.getConfigurationManager().updateIndicators(databaseUUID, passed, failed, warnings, skipped);
  }

  public void updateSIARDPath(String databaseUUID, String siardPath) {
    updateValidationFields(databaseUUID, Pair.of(ViewerConstants.SOLR_DATABASES_SIARD_PATH, siardPath));
  }

  @SafeVarargs
  private final void updateValidationFields(String databaseUUID, Pair<String, ?>... fields) {
    // create document to update this DB
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, databaseUUID);

    // add all the fields that will be updated
    for (Pair<String, ?> field : fields) {
      LOGGER.debug("Updating " + field.getFirst() + " to " + field.getSecond());
      doc.addField(field.getFirst(), SolrUtils.asValueUpdate(field.getSecond()));
    }

    // send it to Solr
    try {
      insertDocument(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME, doc);
    } catch (ViewerException e) {
      LOGGER.error("Could not update SIARD validation information for {}", databaseUUID, e);
    }
  }

  public void updateDatabaseMetadata(String databaseUUID, ViewerMetadata metadata) {
    LOGGER.debug("updateDatabaseMetadata");

    // create document to update this DB
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, databaseUUID);

    try {
      doc.addField(ViewerConstants.SOLR_DATABASES_METADATA,
        SolrUtils.asValueUpdate(JsonTransformer.getJsonFromObject(metadata)));
      insertDocument(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME, doc);
      LOGGER.debug("SUCCESS updateDatabaseMetadata");
    } catch (ViewerException e) {
      LOGGER.error("Could not update database progress for {}", databaseUUID, e);
    }

  }

  public final void addDatabaseField(final String databaseUUID, final String documentUUID,
    List<SolrInputDocument> nestedDocuments, CollectionConfiguration configuration) {
    RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, documentUUID);

    List<String> fields = new ArrayList<>();
    for (SolrInputDocument nest : nestedDocuments) {
      for (SolrInputField field : nest) {
        fields.add((String) field.getValue());
      }
    }

    // add a non-stored field for search only
    doc.addField("token" + ViewerConstants.SOLR_DYN_NEST_MULTI, fields);

    // add nested documents to root document
    doc.addField(ViewerConstants.SOLR_ROWS_NESTED, SolrUtils.addValueUpdate(nestedDocuments));

    try {
      insertDocument(collection.getIndexName(), doc);
    } catch (ViewerException e) {
      LOGGER.error("Could not update database progress for {}", databaseUUID, e);
    }
  }

  public SolrInputDocument createNestedDocument(String uuid, String parentUUID, String tableRowUUID,
    Map<String, ?> fields, String tableId, String nestedUUID) {
    SolrInputDocument nestedDoc = new SolrInputDocument();
    nestedDoc.addField(ViewerConstants.INDEX_ID, uuid);
    nestedDoc.addField(ViewerConstants.SOLR_ROWS_NESTED_UUID, nestedUUID);
    nestedDoc.addField(ViewerConstants.SOLR_ROWS_NESTED_TABLE_ID, tableId);
    nestedDoc.addField("parentUUID_t", parentUUID);
    nestedDoc.addField(ViewerConstants.SOLR_ROWS_NESTED_ORIGINAL_UUID, tableRowUUID);
    for (Map.Entry<String, ?> entry : fields.entrySet()) {
      if (entry.getKey().equals(ViewerConstants.SOLR_ROWS_NESTED)) {
        continue;
      }
      nestedDoc.addField(entry.getKey(), entry.getValue());
    }

    return nestedDoc;
  }

  public void deleteNestedDocuments(String databaseUUID, String documentUUID) {
    RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, documentUUID);
    doc.addField(ViewerConstants.SOLR_ROWS_NESTED, SolrUtils.asValueUpdate(null));
    try {
      insertDocument(collection.getIndexName(), doc);
    } catch (ViewerException e) {
      LOGGER.error("Could not delete nested document for {}", databaseUUID, e);
    }
  }
}
