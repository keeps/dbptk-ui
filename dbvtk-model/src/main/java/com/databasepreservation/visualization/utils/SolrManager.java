package com.databasepreservation.visualization.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.visualization.client.SavedSearch;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabaseFromToolkit;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerRow;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.exceptions.ViewerException;
import com.databasepreservation.visualization.shared.ViewerSafeConstants;
import com.databasepreservation.visualization.transformers.SolrTransformer;

/**
 * Exposes some methods to interact with a Solr Server connected through HTTP
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SolrManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrManager.class);
  private static final long INSERT_DOCUMENT_TIMEOUT = 60000; // 60 seconds
  private static final int MAX_BUFFERED_DOCUMENTS_PER_COLLECTION = 10;
  private static final int MAX_BUFFERED_COLLECTIONS = 10;

  private final HttpSolrClient client;
  private final Set<String> collectionsToCommit;
  // private final LinkedHashMap<String, String> tablesUUIDandName = new
  // LinkedHashMap<>();
  private Map<String, List<SolrInputDocument>> docsByCollection = new HashMap<>();
  private boolean setupDone = false;

  public SolrManager(String url) {
    client = new HttpSolrClient.Builder().withBaseSolrUrl(url).build();
    client.setConnectionTimeout(5000);
    // allowCompression defaults to false.
    // Server side must support gzip or deflate for this to have any effect.
    // solrDBList.setAllowCompression(true);

    // TODO: ensure that solr is running in cloud mode before execution

    collectionsToCommit = new HashSet<>();
  }

  /**
   * Adds a database to the databases collection and asynchronously creates
   * collections for its tables
   * 
   * @param database
   *          the new database
   */
  public void addDatabase(ViewerDatabase database) throws ViewerException {
    // creates databases collection, skipping if it is present
    SolrRequest request = CollectionAdminRequest.createCollection(
      ViewerSafeConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME, ViewerSafeConstants.SOLR_CONFIGSET_DATABASE, 1, 1);
    try {
      NamedList<Object> response = client.request(request);
    } catch (SolrServerException | IOException e) {
      throw new ViewerException("Error creating collection " + ViewerSafeConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME,
        e);
    } catch (SolrException e) {
      if (e.getMessage().contains(
        "collection already exists: " + ViewerSafeConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME)) {
        LOGGER.info("collection " + ViewerSafeConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME + " already exists.");
      } else {
        throw new ViewerException("Error creating collection "
          + ViewerSafeConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME, e);
      }
    }

    // add this database to the collection
    collectionsToCommit.add(ViewerSafeConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME);
    insertDocument(ViewerSafeConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME, SolrTransformer.fromDatabase(database));

    // create collection needed to store saved searches
    try {
      createSavedSearchesCollection();
    } catch (ViewerException e) {
      LOGGER.error("Error creating saved searches collection", e);
    }

    // prepare tables to create the collection
    // for (ViewerSchema viewerSchema : database.getMetadata().getSchemas()) {
    // for (ViewerTable viewerTable : viewerSchema.getTables()) {
    // tablesUUIDandName.put(viewerTable.getUUID(), viewerTable.getName());
    // }
    // }
  }

  public void removeDatabase(ViewerDatabase database) throws ViewerException {
    // delete related table collections
    for (ViewerSchema viewerSchema : database.getMetadata().getSchemas()) {
      for (ViewerTable viewerTable : viewerSchema.getTables()) {
        String collectionName = SolrUtils.getTableCollectionName(viewerTable.getUUID());
        SolrRequest request = CollectionAdminRequest.deleteCollection(collectionName);
        try {
          client.request(request);
          LOGGER.debug("Deleted collection " + collectionName);
        } catch (SolrServerException | IOException | SolrException e) {
          throw new ViewerException("Error deleting collection " + collectionName, e);
        }
      }
    }

    // delete related saved searches
    Filter savedSearchFilter = new Filter(new SimpleFilterParameter(ViewerSafeConstants.SOLR_SEARCHES_DATABASE_UUID,
      database.getUUID()));
    try {
      SolrUtils.delete(client, SavedSearch.class, savedSearchFilter);
      commit(ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, false);
      LOGGER.debug("Deleted saved searches for database " + database.getUUID());
    } catch (GenericException | RequestNotValidException e) {
      throw new ViewerException("Error deleting saved searches for database " + database.getUUID(), e);
    }

    // delete the database item
    try {
      SolrUtils.delete(client, ViewerDatabase.class, Arrays.asList(database.getUUID()));
      commit(ViewerSafeConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME, false);
      LOGGER.debug("Deleted database " + database.getUUID());
    } catch (GenericException e) {
      throw new ViewerException("Error deleting the database " + database.getUUID(), e);
    }
  }

  /**
   * Creates a new table collection in solr for the specified table
   *
   * @param table
   *          the table which data is going to be saved in this collections
   */
  public void addTable(ViewerTable table) throws ViewerException {
    String collectionName = SolrUtils.getTableCollectionName(table.getUUID());
    CollectionAdminRequest.Create request = CollectionAdminRequest.createCollection(collectionName,
      ViewerSafeConstants.SOLR_CONFIGSET_TABLE, 1, 1);

    try {
      LOGGER.info("Creating collection for table " + table.getName() + " with id " + table.getUUID());
      NamedList<Object> response = client.request(request);
      LOGGER.debug("Response from server (create collection for table with id " + table.getUUID() + "): "
        + response.toString());
      Object duration = response.findRecursive("responseHeader", "QTime");
      if (duration != null) {
        LOGGER.info("Created in " + duration + " ms");
      }
    } catch (SolrException e) {
      LOGGER.error("Error in Solr server while creating collection " + collectionName, e);
    } catch (Exception e) {
      // mainly: SolrServerException and IOException
      LOGGER.error("Error creating collection " + collectionName, e);
    }

    collectionsToCommit.add(collectionName);
  }

  public void addRow(ViewerTable table, ViewerRow row) throws ViewerException {
    String collectionName = SolrUtils.getTableCollectionName(table.getUUID());
    insertDocument(collectionName, SolrTransformer.fromRow(table, row));
  }

  /**
   * Commits all changes to all modified collections and optimizes them
   *
   * @throws ViewerException
   */
  public void commitAll() throws ViewerException {
    for (String collection : collectionsToCommit) {
      commitAndOptimize(collection);
    }
    collectionsToCommit.clear();
  }

  /**
   * Frees resources created by this SolrManager object
   *
   * @throws ViewerException
   *           in case some resource could not be closed successfully
   */
  public void freeResources() throws ViewerException {
    try {
      client.close();
    } catch (IOException e) {
      throw new ViewerException(e);
    }
  }

  public <T extends IsIndexed> IndexResult<T> find(Class<T> classToReturn, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets) throws GenericException, RequestNotValidException {
    return SolrUtils.find(client, classToReturn, filter, sorter, sublist, facets);
  }

  public <T extends IsIndexed> Long count(Class<T> classToReturn, Filter filter) throws GenericException,
    RequestNotValidException {
    return SolrUtils.count(client, classToReturn, filter);
  }

  public <T extends IsIndexed> T retrieve(Class<T> classToReturn, String id) throws NotFoundException, GenericException {
    return SolrUtils.retrieve(client, classToReturn, id);
  }

  public <T extends IsIndexed> IndexResult<T> findRows(Class<T> classToReturn, String tableUUID, Filter filter,
    Sorter sorter, Sublist sublist, Facets facets) throws GenericException, RequestNotValidException {
    return SolrUtils.find(client, classToReturn, tableUUID, filter, sorter, sublist, facets);
  }

  public InputStream findRowsCSV(String tableUUID, Filter filter, Sorter sorter, Sublist sublist, List<String> fields)
    throws GenericException, RequestNotValidException {
    return SolrUtils.findCSV(client, SolrUtils.getTableCollectionName(tableUUID), filter, sorter, sublist, fields);
  }

  public <T extends IsIndexed> Long countRows(Class<T> classToReturn, String tableUUID, Filter filter)
    throws GenericException, RequestNotValidException {
    return SolrUtils.count(client, classToReturn, tableUUID, filter);
  }

  public <T extends IsIndexed> T retrieveRows(Class<T> classToReturn, String tableUUID, String rowUUID)
    throws NotFoundException, GenericException {
    return SolrUtils.retrieve(client, classToReturn, tableUUID, rowUUID);
  }

  public void addSavedSearch(SavedSearch savedSearch) throws NotFoundException, GenericException {
    try {
      createSavedSearchesCollection();
    } catch (ViewerException e) {
      LOGGER.error("Error creating saved searches collection", e);
    }

    SolrInputDocument doc = SolrTransformer.fromSavedSearch(savedSearch);

    try {
      client.add(ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, doc);
      client.commit(ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, true, true, true);
    } catch (SolrServerException e) {
      LOGGER.debug("Solr error while attempting to save search", e);
    } catch (IOException e) {
      LOGGER.debug("IOException while attempting to save search", e);
    }
  }

  public void editSavedSearch(String uuid, String name, String description) throws NotFoundException, GenericException {
    try {
      createSavedSearchesCollection();
    } catch (ViewerException e) {
      LOGGER.error("Error creating saved searches collection", e);
    }

    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(ViewerSafeConstants.SOLR_SEARCHES_ID, uuid);
    doc.addField(ViewerSafeConstants.SOLR_SEARCHES_NAME, SolrUtils.asValueUpdate(name));
    doc.addField(ViewerSafeConstants.SOLR_SEARCHES_DESCRIPTION, SolrUtils.asValueUpdate(description));

    try {
      client.add(ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, doc);
      client.commit(ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, true, true);
    } catch (SolrServerException e) {
      LOGGER.debug("Solr error while attempting to save search", e);
    } catch (IOException e) {
      LOGGER.debug("IOException while attempting to save search", e);
    }
  }

  public void deleteSavedSearch(String uuid) throws NotFoundException, GenericException {
    try {
      client.deleteById(ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, uuid);
      client.commit(ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, true, true);
    } catch (SolrServerException e) {
      LOGGER.debug("Solr error while attempting to delete search", e);
    } catch (IOException e) {
      LOGGER.debug("IOException while attempting to delete search", e);
    }
  }

  private void createSavedSearchesCollection() throws ViewerException {
    // creates saved searches collection, skipping if it is present
    CollectionAdminRequest.Create request = CollectionAdminRequest.createCollection(
      ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, ViewerSafeConstants.SOLR_CONFIGSET_SEARCHES, 1, 1);
    try {
      NamedList<Object> response = client.request(request);
    } catch (SolrServerException | IOException e) {
      throw new ViewerException("Error creating collection " + ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME,
        e);
    } catch (SolrException e) {
      if (e.getMessage().contains(
        "collection already exists: " + ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME)) {
        LOGGER.info("collection " + ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME + " already exists.");
      } else {
        throw new ViewerException("Error creating collection "
          + ViewerSafeConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, e);
      }
    }
  }

  private void insertDocument(String collection, SolrInputDocument doc) throws ViewerException {
    if (doc == null) {
      throw new ViewerException("Attempted to insert null document into collection " + collection);
    }

    if (collection.equals(ViewerSafeConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME)) {
      insertDocumentNow(collection, doc);
    } else {
      // add document to buffer
      if (!docsByCollection.containsKey(collection)) {
        docsByCollection.put(collection, new ArrayList<SolrInputDocument>());
      }
      List<SolrInputDocument> docs = docsByCollection.get(collection);
      // LOGGER.info("~~ AddedBatch doc to collection " + collection);
      docs.add(doc);

      // if buffer limit has been reached, "flush" it to solr
      if (docs.size() >= MAX_BUFFERED_DOCUMENTS_PER_COLLECTION || docsByCollection.size() >= MAX_BUFFERED_COLLECTIONS) {
        insertPendingDocuments();
      }
    }
  }

  /**
   * The collections are not immediately available after creation, this method
   * makes sequential attempts to insert a document before giving up (by
   * timeout)
   *
   * @throws ViewerException
   *           in case of a fatal error
   */
  private void insertDocumentNow(String collection, SolrInputDocument doc) throws ViewerException {
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
          LOGGER.warn("Could not insert a document batch in collection " + collection + ". Response: "
            + response.toString());
        }
      } catch (SolrException e) {
        if (e.code() == 404) {
          // this means that the collection does not exist yet. retry
          LOGGER.debug("Collection " + collection + " does not exist (yet). Retrying (" + tries + ")");
        } else {
          LOGGER.warn("Could not insert a document batch in collection" + collection + ". Last response (if any): "
            + String.valueOf(response), e);
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

  /**
   * The collections are not immediately available after creation, this method
   * makes sequential attempts to insert documents (previously stored in a
   * buffer) before giving up (by timeout)
   *
   * @throws ViewerException
   *           in case of a fatal error
   */
  private void insertPendingDocuments() throws ViewerException {
    long timeoutStart = System.currentTimeMillis();
    int tries = 0;
    boolean insertedAllDocuments;
    do {
      UpdateResponse response = null;
      try {
        // add documents, because the buffer limits were reached
        for (String currentCollection : docsByCollection.keySet()) {
          List<SolrInputDocument> docs = docsByCollection.get(currentCollection);
          if (!docs.isEmpty()) {
            try {
              response = client.add(currentCollection, docs);
              if (response.getStatus() == 0) {
                // LOGGER.info("~~ Inserted " + docs.size() +
                // " into collection " + currentCollection);
                collectionsToCommit.add(currentCollection);
                docs.clear();
                // reset the timeout when something is inserted
                timeoutStart = System.currentTimeMillis();
              } else {
                LOGGER.warn("Could not insert a document batch in collection " + currentCollection + ". Response: "
                  + response.toString());
              }
            } catch (SolrException e) {
              if (e.code() == 404) {
                // this means that the collection does not exist yet. retry
                LOGGER.debug("Collection " + currentCollection + " does not exist (yet). Retrying (" + tries + ")");
              } else {
                LOGGER.warn("Could not insert a document batch in collection" + currentCollection
                  + ". Last response (if any): " + String.valueOf(response), e);
              }
            }
          }
        }
      } catch (SolrServerException | SolrException | IOException e) {
        throw new ViewerException("Problem adding information", e);
      }

      // check if something still needs to be inserted
      insertedAllDocuments = true;
      for (List<SolrInputDocument> docs : docsByCollection.values()) {
        if (!docs.isEmpty()) {
          insertedAllDocuments = false;
          break;
        }
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

    if (insertedAllDocuments) {
      // remove empty or null lists
      for (Iterator<String> iter = docsByCollection.keySet().iterator(); iter.hasNext();) {
        String key = iter.next();
        if (docsByCollection.get(key) == null || docsByCollection.get(key).isEmpty()) {
          iter.remove();
        }
      }

    } else {
      throw new ViewerException(
        "Could not insert a document batch in collection. Reason: Timeout reached while waiting for collection to be available.");
    }
  }

  private void commitAndOptimize(String collection) throws ViewerException {
    // insert any pending documents before optimizing
    insertPendingDocuments();
    commit(collection, true);
  }

  /**
   * The collections are not immediately available after creation, this method
   * makes sequential attempts to commit (and possibly optimize) a collection
   * before giving up (by timeout)
   *
   * @throws ViewerException
   *           in case of a fatal error
   */
  private void commit(String collection, boolean optimize) throws ViewerException {
    long timeoutStart = System.currentTimeMillis();
    int tries = 0;
    while (System.currentTimeMillis() - timeoutStart < INSERT_DOCUMENT_TIMEOUT) {
      UpdateResponse response;
      try {
        // commit
        try {
          response = client.commit(collection);
          if (response.getStatus() != 0) {
            throw new ViewerException("Could not commit collection " + collection);
          }
        } catch (SolrServerException | IOException e) {
          throw new ViewerException("Problem committing collection " + collection, e);
        }

        if (optimize) {
          try {
            response = client.optimize(collection);
            if (response.getStatus() == 0) {
              return;
            } else {
              throw new ViewerException("Could not optimize collection " + collection);
            }
          } catch (SolrServerException | IOException e) {
            throw new ViewerException("Problem optimizing collection " + collection, e);
          }
        } else {
          return;
        }
      } catch (SolrException e) {
        if (e.code() == 404) {
          // this means that the collection does not exist yet. retry
          LOGGER.debug("Collection " + collection + " does not exist. Retrying (" + tries + ")");
        } else {
          throw new ViewerException("Problem committing collection " + collection, e);
        }
      }

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        LOGGER.debug("insertDocument sleep was interrupted", e);
      }
      tries++;
    }

    // INSERT_DOCUMENT_TIMEOUT reached
    if (optimize) {
      throw new ViewerException("Failed to commit and optimize collection " + collection
        + ". Reason: Timeout reached while waiting for collection to be available, ran " + tries + " attempts.");
    } else {
      throw new ViewerException("Failed to commit collection " + collection
        + ". Reason: Timeout reached while waiting for collection to be available, ran " + tries + " attempts.");
    }

  }

  public void markDatabaseAsReady(ViewerDatabaseFromToolkit viewerDatabase) throws ViewerException {
    updateDatabaseFields(viewerDatabase.getUUID(), new Pair<>(ViewerSafeConstants.SOLR_DATABASE_STATUS,
      ViewerDatabase.Status.AVAILABLE.toString()));
  }

  private void updateDatabaseFields(String databaseUUID, Pair<String, Object>... fields) {
    // create document to update this DB
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerSafeConstants.SOLR_DATABASE_ID, databaseUUID);

    // add all the fields that will be updated
    for (Pair<String, Object> field : fields) {
      LOGGER.debug("Updating " + field.getFirst() + " to " + field.getSecond());
      doc.addField(field.getFirst(), SolrUtils.asValueUpdate(field.getSecond()));
    }

    // send it to Solr
    try {
      insertDocument(ViewerSafeConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME, doc);
    } catch (ViewerException e) {
      LOGGER.error("Could not update database progress for {}", databaseUUID, e);
    }
  }

  public void updateDatabaseTotalSchemas(String databaseUUID, int totalSchemas) {
    updateDatabaseFields(databaseUUID, new Pair<>(ViewerSafeConstants.SOLR_DATABASE_TOTAL_SCHEMAS, totalSchemas));
  }

  public void updateDatabaseCurrentSchema(String databaseUUID, String schemaName, long completedSchemas, int totalTables) {
    updateDatabaseFields(databaseUUID, new Pair<>(ViewerSafeConstants.SOLR_DATABASE_CURRENT_SCHEMA_NAME, schemaName),
      new Pair<>(ViewerSafeConstants.SOLR_DATABASE_INGESTED_SCHEMAS, completedSchemas), new Pair<>(
        ViewerSafeConstants.SOLR_DATABASE_TOTAL_TABLES, totalTables));
  }

  public void updateDatabaseCurrentTable(String databaseUUID, String tableName, long completedTablesInSchema,
    long totalRows) {
    updateDatabaseFields(databaseUUID, new Pair<>(ViewerSafeConstants.SOLR_DATABASE_CURRENT_TABLE_NAME, tableName),
      new Pair<>(ViewerSafeConstants.SOLR_DATABASE_INGESTED_TABLES, completedTablesInSchema), new Pair<>(
        ViewerSafeConstants.SOLR_DATABASE_TOTAL_ROWS, totalRows), new Pair<>(
        ViewerSafeConstants.SOLR_DATABASE_INGESTED_ROWS, 0));
  }

  public void updateDatabaseCurrentRow(String databaseUUID, long completedRows) {
    updateDatabaseFields(databaseUUID, new Pair<>(ViewerSafeConstants.SOLR_DATABASE_INGESTED_ROWS, completedRows));
  }

  public void updateDatabaseIngestionFinished(String databaseUUID) {
    updateDatabaseFields(databaseUUID, new Pair<>(ViewerSafeConstants.SOLR_DATABASE_TOTAL_SCHEMAS, null), new Pair<>(
      ViewerSafeConstants.SOLR_DATABASE_CURRENT_SCHEMA_NAME, null), new Pair<>(
      ViewerSafeConstants.SOLR_DATABASE_INGESTED_SCHEMAS, null), new Pair<>(
      ViewerSafeConstants.SOLR_DATABASE_TOTAL_TABLES, null), new Pair<>(
      ViewerSafeConstants.SOLR_DATABASE_CURRENT_TABLE_NAME, null), new Pair<>(
      ViewerSafeConstants.SOLR_DATABASE_INGESTED_TABLES, null), new Pair<>(
      ViewerSafeConstants.SOLR_DATABASE_TOTAL_ROWS, null), new Pair<>(ViewerSafeConstants.SOLR_DATABASE_INGESTED_ROWS,
      null));
  }
}
