/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.index;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.server.index.schema.collections.DatabasesCollection;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.joda.time.DateTime;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.api.exceptions.IllegalAccessException;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.authorization.AuthorizationDetails;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseFromToolkit;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.exceptions.SavedSearchException;
import com.databasepreservation.common.exceptions.ViewerException;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.batch.context.JobContext;
import com.databasepreservation.common.server.index.schema.SolrCollection;
import com.databasepreservation.common.server.index.schema.SolrDefaultCollectionRegistry;
import com.databasepreservation.common.server.index.schema.SolrRowsCollectionRegistry;
import com.databasepreservation.common.server.index.schema.collections.RowsCollection;
import com.databasepreservation.common.server.index.utils.IterableDatabaseResult;
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

  public void addDatabaseRowCollection(final CollectionStatus collectionStatus) throws ViewerException {
    updateValidationFields(collectionStatus.getDatabaseUUID(),
      Pair.of(ViewerConstants.SOLR_DATABASES_STATUS, ViewerDatabaseStatus.INGESTING.toString()));
    RowsCollection collection = new RowsCollection(collectionStatus);
    collection.createRowsCollection();
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
  }

  public void addRow(CollectionStatus collectionStatus, ViewerRow row) throws ViewerException {
    RowsCollection collection = SolrRowsCollectionRegistry.get(collectionStatus.getDatabaseUUID());

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
      fieldsToReturn, new HashMap<>());
  }

  public <T extends IsIndexed> IndexResult<T> find(Class<T> classToReturn, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, List<String> fieldsToReturn, List<Filter> filterQueries)
    throws GenericException, RequestNotValidException {
    return SolrUtils.find(client, SolrDefaultCollectionRegistry.get(classToReturn), filter, sorter, sublist, facets,
      fieldsToReturn, new HashMap<>(), filterQueries, "lucene", List.of(), false, List.of());
  }

  public <T extends IsIndexed> IndexResult<T> find(Class<T> classToReturn, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets, List<String> fieldsToReturn, String defType, List<Filter> filterQueries,
    List<String> queryFields) throws GenericException, RequestNotValidException {
    return SolrUtils.find(client, SolrDefaultCollectionRegistry.get(classToReturn), filter, sorter, sublist, facets,
      fieldsToReturn, new HashMap<>(), filterQueries, defType, queryFields, false, List.of());
  }

  public <T extends IsIndexed> IndexResult<T> findHits(Class<T> classToReturn, String alias, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets) throws GenericException, RequestNotValidException {
    return SolrUtils.findHits(client, SolrDefaultCollectionRegistry.get(classToReturn), alias, filter, sorter, sublist,
      facets, "lucene", List.of());
  }

  public <T extends IsIndexed> IndexResult<T> findHits(Class<T> classToReturn, String alias, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets, String defType, List<String> queryFields)
    throws GenericException, RequestNotValidException {
    return SolrUtils.findHits(client, SolrDefaultCollectionRegistry.get(classToReturn), alias, filter, sorter, sublist,
      facets, defType, queryFields);
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
    return SolrUtils.findRows(client, databaseUUID, filter, sorter, sublist, facets, fieldsToReturn, new HashMap<>());
  }

  public IndexResult<ViewerRow> findRows(String databaseUUID, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, List<String> fieldsToReturn, Map<String, String> extraParameters)
    throws GenericException, RequestNotValidException {
    return SolrUtils.findRows(client, databaseUUID, filter, sorter, sublist, facets, fieldsToReturn, extraParameters);
  }

  public Pair<IndexResult<ViewerRow>, String> findRows(String databaseUUID, Filter filter, Sorter sorter, int pageSize,
    String cursorMark, List<String> fieldsToReturn, Map<String, String> extraParameters)
    throws GenericException, RequestNotValidException {
    return SolrUtils.findRows(client, databaseUUID, filter, sorter, pageSize, cursorMark, fieldsToReturn,
      extraParameters);
  }

  public IndexResult<ViewerRow> findRows(String databaseUUID, Filter filter, Sorter sorter, Sublist sublist,
    Facets facets, List<String> fieldsToReturn, Map<String, String> extraParameters, String defType, Filter filterQuery,
    List<String> queryFields, boolean highlighing, List<String> highlightedFields)
    throws GenericException, RequestNotValidException {
    return SolrUtils.findRows(client, databaseUUID, filter, sorter, sublist, facets, fieldsToReturn, extraParameters,
      defType, filterQuery, queryFields, highlighing, highlightedFields);
  }

  public IterableIndexResult findAllRows(String databaseUUID, final Filter filter, final Sorter sorter,
    final List<String> fieldsToReturn) {
    return findAllRows(databaseUUID, filter, sorter, fieldsToReturn, new HashMap<>());
  }

  public IterableIndexResult findAllRows(String databaseUUID, final Filter filter, final Sorter sorter,
    final List<String> fieldsToReturn, Map<String, String> extraParameters) {
    return new IterableIndexResult(client, databaseUUID, filter, sorter, fieldsToReturn, extraParameters);
  }

  public IterableNestedIndexResult findAllRows(String databaseUUID, SolrQuery query, final Sorter sorter) {
    return new IterableNestedIndexResult(client, databaseUUID, query, sorter);
  }

  public <T extends IsIndexed> IterableDatabaseResult<T> findAll(Class<T> classToReturn, Filter filter, Sorter sorter,
    List<String> fieldsToReturn) {
    return new IterableDatabaseResult<>(client, classToReturn, filter, sorter, fieldsToReturn, new ArrayList<>());
  }

  public <T extends IsIndexed> IterableDatabaseResult<T> findAll(Class<T> classToReturn, Filter filter, Sorter sorter,
    List<String> fieldsToReturn, List<Filter> filterQueries) {
    return new IterableDatabaseResult<>(client, classToReturn, filter, sorter, fieldsToReturn, filterQueries);
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

  public ViewerRow retrieveNestedRows(String databaseUUID, String rowUUID) throws NotFoundException, GenericException {
    return SolrUtils.retrieveRows(client, databaseUUID, rowUUID);
  }

  public void addLogEntry(ActivityLogEntry logEntry) throws NotFoundException, GenericException {
    SolrCollection<ActivityLogEntry> activityLogEntrySolrCollection = SolrDefaultCollectionRegistry
      .get(ActivityLogEntry.class);
    try {
      SolrInputDocument doc = activityLogEntrySolrCollection.toSolrDocument(logEntry);
      client.add(activityLogEntrySolrCollection.getIndexName(), doc);
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
      client.add(viewerJobSolrCollection.getIndexName(), doc, 1000);
    } catch (ViewerException | AuthorizationDeniedException | RequestNotValidException e) {
      LOGGER.debug("Solr error while converting to document", e);
    } catch (SolrServerException e) {
      LOGGER.debug("Solr error while attempting to save batch job", e);
    } catch (IOException e) {
      LOGGER.debug("IOException while attempting to save batch job", e);
    }
  }

  public void editBatchJob(String jobUUID, long countRows, long processedRows, long skipCount, JobContext jobContext) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, jobUUID);
    doc.addField(ViewerConstants.SOLR_BATCH_JOB_ROWS_TO_PROCESS, SolrUtils.asValueUpdate(countRows));
    doc.addField(ViewerConstants.SOLR_BATCH_JOB_ROWS_PROCESSED, SolrUtils.asValueUpdate(processedRows));
    doc.addField(ViewerConstants.SOLR_BATCH_JOB_SKIP_COUNT, SolrUtils.asValueUpdate(skipCount));

    doc.addField(ViewerConstants.SOLR_BATCH_JOB_CURRENT_STEP_NAME,
      SolrUtils.asValueUpdate(jobContext.getCurrentStepName()));
    doc.addField(ViewerConstants.SOLR_BATCH_JOB_CURRENT_STEP_NUMBER,
      SolrUtils.asValueUpdate(jobContext.getCurrentStepNumber()));
    doc.addField(ViewerConstants.SOLR_BATCH_JOB_TOTAL_STEPS, SolrUtils.asValueUpdate(jobContext.getTotalSteps()));
    try {
      insertDocument(ViewerConstants.SOLR_INDEX_BATCH_JOBS_COLLECTION_NAME, doc);
    } catch (ViewerException e) {
      LOGGER.debug("Solr error while converting to document", e);
    }
  }

  public void editBatchJob(ViewerJob job) throws NotFoundException, GenericException {
    SolrCollection<ViewerJob> viewerJobSolrCollection = SolrDefaultCollectionRegistry.get(ViewerJob.class);
    try {
      SolrInputDocument doc = new SolrInputDocument();
      doc.addField(ViewerConstants.INDEX_ID, job.getUuid());
      for (SolrInputField field : viewerJobSolrCollection.toSolrDocument(job)) {
        if (!field.getName().equals(ViewerConstants.INDEX_ID) && field.getValue() != null) {
          doc.addField(field.getName(), SolrUtils.asValueUpdate(field.getValue()));
        }
      }
      insertDocument(ViewerConstants.SOLR_INDEX_BATCH_JOBS_COLLECTION_NAME, doc);
    } catch (ViewerException | AuthorizationDeniedException | RequestNotValidException e) {
      LOGGER.debug("Solr error while converting to document", e);
    }
  }

  public void deleteBatchJob() throws GenericException {
    try {
      SolrUtils.delete(client, SolrDefaultCollectionRegistry.get(ViewerJob.class), new Filter());
    } catch (RequestNotValidException e) {
      LOGGER.debug("Solr error while deleting document", e);
    }
  }

  public void addSavedSearch(SavedSearch savedSearch)
    throws NotFoundException, GenericException, IllegalAccessException {
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

  public void editSavedSearch(String databaseUUID, String uuid, String name, String description)
    throws SavedSearchException, IllegalAccessException {
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
        response = client.add(collection, doc);
        if (response.getStatus() == 0) {
          insertedAllDocuments = true;
          break;
        } else {
          LOGGER.warn("Could not insert a document batch in collection {}. Response: {}", collection, response);
        }
      } catch (SolrException e) {
        if (e.code() == 404) {
          // this means that the collection does not exist yet. retry
          LOGGER.debug("Collection {} does not exist (yet). Retrying ({})", collection, tries);
        } else {
          LOGGER.warn("Could not insert a document batch in collection {}. Last response (if any): {}", collection,
            response, e);
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

  public void markDatabaseAsReady(final String databaseUUID) throws ViewerException, GenericException {
    updateDatabaseFields(databaseUUID,
      Pair.of(ViewerConstants.SOLR_DATABASES_STATUS, ViewerDatabaseStatus.AVAILABLE.toString()),
      Pair.of(ViewerConstants.SOLR_DATABASES_BROWSE_LOAD_DATE, new DateTime().toString()));
    ViewerFactory.getConfigurationManager().updateDatabaseStatus(databaseUUID, ViewerDatabaseStatus.AVAILABLE);
  }

  @SafeVarargs
  private final void updateDatabaseFields(String databaseUUID, Pair<String, ?>... fields) {
    // create document to update this DB
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, databaseUUID);

    // add all the fields that will be updated
    for (Pair<String, ?> field : fields) {
      LOGGER.debug("Updating {} to {}", field.getFirst(), field.getSecond());
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

  public void updateSIARDValidationIndicators(String databaseUUID, String passed, String failed, String errors,
    String warnings, String skipped) {
    updateValidationFields(databaseUUID, Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATION_PASSED, passed),
      Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATION_FAILED, failed),
      Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATION_ERRORS, errors),
      Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATION_WARNINGS, warnings),
      Pair.of(ViewerConstants.SOLR_DATABASES_VALIDATION_SKIPPED, skipped));
    // delegate
    ViewerFactory.getConfigurationManager().updateIndicators(databaseUUID, passed, failed, warnings, skipped, errors);
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
      LOGGER.debug("Updating {} to {}", field.getFirst(), field.getSecond());
      doc.addField(field.getFirst(), SolrUtils.asValueUpdate(field.getSecond()));
    }

    // send it to Solr
    try {
      insertDocument(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME, doc);
    } catch (ViewerException e) {
      LOGGER.error("Could not update SIARD validation information for {}", databaseUUID, e);
    }
  }

  public void updateDatabasePermissions(String databaseUUID, Map<String, AuthorizationDetails> permissions)
    throws GenericException, ViewerException {
    LOGGER.debug("Starting to update database permissions ({})", databaseUUID);
    // create document to update this DB
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, databaseUUID);

    try {
      List<SolrInputDocument> permissionsDocs = new ArrayList<>();
      for (Map.Entry<String, AuthorizationDetails> permissionEntry : permissions.entrySet()) {
        SolrInputDocument childPermissionsDoc = new SolrInputDocument();
        AuthorizationDetails authorizationDetails = permissionEntry.getValue();
        childPermissionsDoc.addField(ViewerConstants.SOLR_DATABASES_PERMISSIONS_GROUP, permissionEntry.getKey());
        childPermissionsDoc.addField(ViewerConstants.SOLR_DATABASES_PERMISSIONS_EXPIRY,
          authorizationDetails.getExpiry());
        childPermissionsDoc.addField(ViewerConstants.SOLR_CONTENT_TYPE,
          ViewerConstants.SOLR_DATABASES_CONTENT_TYPE_PERMISSION);
        childPermissionsDoc.addField(ViewerConstants.SOLR_DATABASES_STATUS, ViewerConstants.SOLR_DATABASES_STATUS);

        permissionsDocs.add(childPermissionsDoc);
      }
      doc.addField(ViewerConstants.SOLR_DATABASES_PERMISSIONS, SolrUtils.asValueUpdate(permissionsDocs));
      insertDocument(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME, doc);
    } catch (ViewerException e) {
      LOGGER.error("Could not update database metadata ({})", databaseUUID, e);
    }

    ViewerFactory.getConfigurationManager().updateDatabasePermissions(databaseUUID, permissions);
    LOGGER.debug("Finish updating database metadata ({})", databaseUUID);
  }

  public boolean updateDatabaseSearchAllAvailability(String databaseUUID, boolean isAvailableToSearchAll)
    throws GenericException, ViewerException {
    LOGGER.debug("Starting to update database search all availability ({})", databaseUUID);
    // create document to update this DB
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, databaseUUID);

    try {
      doc.addField(ViewerConstants.SOLR_DATABASES_AVAILABLE_TO_SEARCH_ALL,
        SolrUtils.asValueUpdate(isAvailableToSearchAll));
      insertDocument(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME, doc);
    } catch (ViewerException e) {
      LOGGER.error("Could not update database metadata ({})", databaseUUID, e);
    }

    ViewerFactory.getConfigurationManager().updateDatabaseSearchAllAvailability(databaseUUID, isAvailableToSearchAll);
    LOGGER.debug("Finish updating database metadata ({})", databaseUUID);
    return true;
  }

  public void updateDatabaseMetadata(String databaseUUID, ViewerMetadata metadata) throws GenericException {
    LOGGER.debug("Starting to update database metadata ({})", databaseUUID);

    try {
      SolrDocument existingDoc = client.getById(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME, databaseUUID);

      SolrInputDocument doc = new SolrInputDocument();
      doc.addField(ViewerConstants.INDEX_ID, databaseUUID);

      if (existingDoc != null && existingDoc.containsKey(ViewerConstants.SOLR_DATABASES_METADATA)) {
        //deprecated
        doc.addField(ViewerConstants.SOLR_DATABASES_METADATA,
          SolrUtils.asValueUpdate(JsonTransformer.getJsonFromObject(metadata)));
      } else {
        if (metadata != null) {
          DatabasesCollection.populateMetadataInDocument(metadata, doc, true);
        }
      }

      insertDocument(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME, doc);
      ViewerFactory.getConfigurationManager().updateDatabaseMetadata(databaseUUID, metadata);
      LOGGER.debug("Finish updating database metadata ({})", databaseUUID);
    } catch (GenericException | ViewerException | IOException | SolrServerException e) {
      LOGGER.error("Could not update database metadata ({})", databaseUUID, e);
      throw new GenericException("Could not update database metadata " + databaseUUID , e);
    }

  }

  // Bulk processing methods

  /**
   * Saves a collection of items to Solr using bulk processing. This is the entry
   * point for StepWriters to perform Upserts.
   */
  public <T extends IsIndexed> void insertBatchDocuments(String databaseUUID, List<? extends T> items)
    throws ViewerException {
    if (items == null || items.isEmpty()) {
      return;
    }

    Class<?> objClass = items.get(0).getClass();
    List<SolrInputDocument> docs = new ArrayList<>(items.size());
    String targetCollection;

    if (ViewerRow.class.isAssignableFrom(objClass)) {
      RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);
      targetCollection = collection.getIndexName();

      for (T item : items) {
        docs.add(toAtomicSolrDoc((ViewerRow) item));
      }
    } else {
      @SuppressWarnings("unchecked")
      SolrCollection<T> solrCollection = SolrDefaultCollectionRegistry.get((Class<T>) objClass);
      targetCollection = solrCollection.getIndexName();

      for (T item : items) {
        try {
          docs.add(solrCollection.toSolrDocument(item));
        } catch (Exception e) {
          LOGGER.error("Failed to map item to Solr document: {}", item, e);
        }
      }
    }

    executeBulkUpdate(targetCollection, docs);
  }

  public final void clearExtractedLobTextField(final String databaseUUID, final String documentUUID,
    final String lobFieldName) {

    RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, documentUUID);
    doc.addField(
      ViewerConstants.SOLR_ROWS_OCR_PREFIX + "_" + lobFieldName + "_" + ViewerConstants.SOLR_ROWS_EXTRACTED_TEXT_SUFFIX,
      SolrUtils.setValueUpdate(null));

    try {
      insertDocument(collection.getIndexName(), doc);
    } catch (ViewerException e) {
      LOGGER.error("Could not update row {} for database {}", documentUUID, databaseUUID, e);
    }
  }

  public final void addExtractedTextField(final String databaseUUID, final String documentUUID,
    final String lobFieldName, String extractedText) {

    RowsCollection collection = SolrRowsCollectionRegistry.get(databaseUUID);
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, documentUUID);
    doc.addField(
      ViewerConstants.SOLR_ROWS_OCR_PREFIX + "_" + lobFieldName + "_" + ViewerConstants.SOLR_ROWS_EXTRACTED_TEXT_SUFFIX,
      SolrUtils.addValueUpdate(extractedText));

    try {
      insertDocument(collection.getIndexName(), doc);
    } catch (ViewerException e) {
      LOGGER.error("Could not update row {} for database {}", documentUUID, databaseUUID, e);
    }
  }

  /**
   * Maps a ViewerRow to an Atomic Update document to prevent data loss of fields
   * not present in the current processing chunk.
   */
  private SolrInputDocument toAtomicSolrDoc(ViewerRow row) {
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, row.getUuid());

    if (row.getCells() != null) {
      for (Map.Entry<String, ViewerCell> entry : row.getCells().entrySet()) {
        String fieldName = entry.getKey();

        if (isSystemField(fieldName)) {
          continue;
        }

        Object value = (entry.getValue() != null) ? entry.getValue().getValue() : null;
        doc.addField(fieldName, SolrUtils.asValueUpdate(value));
      }
    }

    if (row.getNestedRowList() != null && !row.getNestedRowList().isEmpty()) {
      addNestedSolrDocument(row, doc);
    }

    return doc;
  }

  /**
   * Internal helper to identify fields that should not be updated atomically.
   */
  private boolean isSystemField(String fieldName) {
    return ViewerConstants.INDEX_ID.equals(fieldName) || ViewerConstants.SOLR_ROWS_TABLE_ID.equals(fieldName);
  }

  /**
   * Executes a high-performance bulk update to a Solr collection.
   * <p>
   * This method is specifically designed for Spring Batch or high-volume indexing
   * operations. It implements a retry mechanism to handle transient Solr errors,
   * such as "Collection Not Found (404)" errors that occur immediately after
   * database ingestion starts. Unlike standard single updates, this method
   * prioritizes fail-fast behavior for critical network or server failures to
   * allow the Batch framework to manage retries according to the defined Step
   * policy.
   * </p>
   *
   * @param collection
   *          the target Solr collection name
   * @param docs
   *          the list of Solr documents to be indexed in a single request
   * @throws ViewerException
   *           if the update fails after the timeout limit or if a fatal IO error
   *           occurs
   */
  private void executeBulkUpdate(String collection, List<SolrInputDocument> docs) throws ViewerException {
    long timeoutLimit = System.currentTimeMillis() + INSERT_DOCUMENT_TIMEOUT;
    boolean insertedAllDocuments = false;

    do {
      try {
        UpdateResponse response = client.add(collection, docs);
        if (response.getStatus() == 0) {
          insertedAllDocuments = true;
        }
      } catch (SolrException e) {
        if (e.code() == 404) {
          LOGGER.debug("Target collection {} not yet available. Retrying...", collection);
        } else {
          throw new ViewerException("Unexpected Solr error during bulk update", e);
        }
      } catch (SolrServerException | IOException e) {
        throw new ViewerException("Connection failure while updating Solr", e);
      }

      if (!insertedAllDocuments) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    } while (!insertedAllDocuments && System.currentTimeMillis() < timeoutLimit);

    if (!insertedAllDocuments) {
      throw new ViewerException("Bulk update timed out for collection: " + collection);
    }
  }

  private static void addNestedSolrDocument(ViewerRow row, SolrInputDocument doc) {
    List<SolrInputDocument> solrNestedDocs = new ArrayList<>();
    List<String> searchTokens = new ArrayList<>();

    for (ViewerRow nestedRow : row.getNestedRowList()) {
      SolrInputDocument nestedDoc = new SolrInputDocument();

      // Mandatory metadata of the nested document
      nestedDoc.addField(ViewerConstants.INDEX_ID, SolrUtils.randomUUID());
      nestedDoc.addField(ViewerConstants.SOLR_ROWS_NESTED_UUID, nestedRow.getNestedUUID());
      nestedDoc.addField(ViewerConstants.SOLR_ROWS_NESTED_TABLE_ID, nestedRow.getNestedTableId());
      nestedDoc.addField(ViewerConstants.SOLR_ROWS_NESTED_ORIGINAL_UUID, nestedRow.getNestedOriginalUUID());

      // Adds the cells of the nested document and generates search tokens
      for (Map.Entry<String, ViewerCell> cellEntry : nestedRow.getCells().entrySet()) {
        String key = cellEntry.getKey();
        String value = cellEntry.getValue() != null ? cellEntry.getValue().getValue() : null;

        nestedDoc.addField(key, value);
        if (value != null) {
          searchTokens.add(value);
        }
      }
      solrNestedDocs.add(nestedDoc);
    }

    // Add the search tokens to the parent document for better searchability and the
    // nested documents as a nested field
    doc.addField("token" + ViewerConstants.SOLR_DYN_NEST_MULTI, SolrUtils.asValueUpdate(searchTokens));
    doc.addField("type" + ViewerConstants.SOLR_DYN_TEXT_GENERAL, SolrUtils.asValueUpdate("parent"));
    doc.addField(ViewerConstants.SOLR_ROWS_NESTED, SolrUtils.asValueUpdate(solrNestedDocs));
  }
}
