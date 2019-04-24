package com.databasepreservation.visualization.server.index;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.common.Pair;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.utils.FileUtils;
import com.databasepreservation.visualization.exceptions.ViewerException;
import com.databasepreservation.visualization.server.index.schema.SolrCollection;
import com.databasepreservation.visualization.server.index.schema.SolrDefaultCollectionRegistry;
import com.databasepreservation.visualization.server.index.schema.SolrRowsCollectionRegistry;
import com.databasepreservation.visualization.server.index.schema.collections.RowsCollection;
import com.databasepreservation.visualization.server.index.utils.SolrUtils;
import com.databasepreservation.visualization.shared.SavedSearch;
import com.databasepreservation.visualization.shared.ViewerConstants;
import com.databasepreservation.visualization.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerDatabaseFromToolkit;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerRow;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerTable;

/**
 * Exposes some methods to interact with a Solr Server
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SolrManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrManager.class);
  private static final long INSERT_DOCUMENT_TIMEOUT = 60000; // 60 seconds
  private static final int MAX_BUFFERED_DOCUMENTS_PER_COLLECTION = 10;
  private static final int MAX_BUFFERED_COLLECTIONS = 10;

  private final CloudSolrClient client;
  private final Set<String> collectionsToCommit = ConcurrentHashMap.newKeySet();
  // private final LinkedHashMap<String, String> tablesUUIDandName = new
  // LinkedHashMap<>();
  private Map<String, Queue<SolrInputDocument>> docsByCollection = new ConcurrentHashMap<>();

  public SolrManager(CloudSolrClient client) {
    this.client = client;
  }

  /**
   * Adds a database to the databases collection
   * 
   * @param database
   *          the new database
   */
  public void addDatabase(ViewerDatabase database) throws ViewerException {
    // add this database to the collection
    collectionsToCommit.add(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME);
    insertDocument(ViewerDatabase.class, database);

    RowsCollection collection = new RowsCollection(database.getUUID());
    collection.createRowsCollection();
    collectionsToCommit.add(collection.getIndexName());
  }

  public void removeDatabase(ViewerDatabase database, Path lobFolder) throws ViewerException {
    // delete the LOBs
    if (lobFolder != null) {
      try {
        FileUtils.deleteDirectoryRecursiveQuietly(lobFolder.resolve(database.getUUID()));
      } catch (InvalidPathException e) {
        throw new ViewerException("Error deleting LOBs for database " + database.getUUID(), e);
      }
    }

    // delete related rows collection
    String rowsCollectionName = SolrRowsCollectionRegistry.get(database.getUUID()).getIndexName();
    SolrRequest<?> request = CollectionAdminRequest.deleteCollection(rowsCollectionName);
    try {
      client.request(request);
      LOGGER.debug("Deleted collection {}", rowsCollectionName);
    } catch (SolrServerException | IOException | SolrException e) {
      throw new ViewerException("Error deleting collection " + rowsCollectionName, e);
    }

    // delete related saved searches
    Filter savedSearchFilter = new Filter(
      new SimpleFilterParameter(ViewerConstants.SOLR_SEARCHES_DATABASE_UUID, database.getUUID()));
    try {
      SolrUtils.delete(client, SolrDefaultCollectionRegistry.get(SavedSearch.class), savedSearchFilter);
      commit(ViewerConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, false);
      LOGGER.debug("Deleted saved searches for database {}", database.getUUID());
    } catch (GenericException | RequestNotValidException e) {
      throw new ViewerException("Error deleting saved searches for database " + database.getUUID(), e);
    }

    // delete the database item
    try {
      SolrUtils.delete(client, SolrDefaultCollectionRegistry.get(ViewerDatabase.class),
        Arrays.asList(database.getUUID()));
      commit(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME, false);
      LOGGER.debug("Deleted database {}", database.getUUID());
    } catch (GenericException e) {
      throw new ViewerException("Error deleting the database " + database.getUUID(), e);
    }
  }

  /**
   * Does nothing. Just a part of the database traversal
   *
   * @param table
   *          the table
   */
  public void addTable(ViewerTable table) throws ViewerException {
  }

  public void addRow(ViewerDatabaseFromToolkit viewerDatabase, ViewerRow row) throws ViewerException {

    RowsCollection collection = SolrRowsCollectionRegistry.get(viewerDatabase.getUUID());

    try {
      insertDocument(collection.getIndexName(), collection.toSolrDocument(row));
    } catch (RequestNotValidException | GenericException | NotFoundException | AuthorizationDeniedException e) {
      throw new ViewerException(e);
    }
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

  public <T extends IsIndexed> IndexResult<T> find(Class<T> classToReturn, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets) throws GenericException, RequestNotValidException {
    return SolrUtils.find(client, SolrDefaultCollectionRegistry.get(classToReturn), filter, sorter, sublist, facets);
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

  public InputStream findRowsCSV(String databaseUUID, Filter filter, Sorter sorter, Sublist sublist,
    List<String> fields) throws GenericException, RequestNotValidException {
    return SolrUtils.findCSV(client, SolrRowsCollectionRegistry.get(databaseUUID).getIndexName(), filter, sorter,
      sublist, fields);
  }

  public <T extends IsIndexed> Long countRows(String databaseUUID, Filter filter)
    throws GenericException, RequestNotValidException {
    return SolrUtils.countRows(client, databaseUUID, filter);
  }

  public ViewerRow retrieveRows(String databaseUUID, String rowUUID) throws NotFoundException, GenericException {
    return SolrUtils.retrieveRows(client, databaseUUID, rowUUID);
  }

  public void addSavedSearch(SavedSearch savedSearch) throws NotFoundException, GenericException {
    SolrCollection<SavedSearch> savedSearchesCollection = SolrDefaultCollectionRegistry.get(SavedSearch.class);

    try {
      SolrInputDocument doc = savedSearchesCollection.toSolrDocument(savedSearch);
      client.add(savedSearchesCollection.getIndexName(), doc);
      client.commit(savedSearchesCollection.getIndexName(), true, true, true);
    } catch (ViewerException | RequestNotValidException | AuthorizationDeniedException e) {
      LOGGER.debug("Solr error while converting to document", e);
    } catch (SolrServerException e) {
      LOGGER.debug("Solr error while attempting to save search", e);
    } catch (IOException e) {
      LOGGER.debug("IOException while attempting to save search", e);
    }
  }

  public void editSavedSearch(String uuid, String name, String description) throws NotFoundException, GenericException {
    SolrInputDocument doc = new SolrInputDocument();

    doc.addField(ViewerConstants.INDEX_ID, uuid);
    doc.addField(ViewerConstants.SOLR_SEARCHES_NAME, SolrUtils.asValueUpdate(name));
    doc.addField(ViewerConstants.SOLR_SEARCHES_DESCRIPTION, SolrUtils.asValueUpdate(description));

    try {
      client.add(ViewerConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, doc);
      client.commit(ViewerConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, true, true);
    } catch (SolrServerException e) {
      LOGGER.debug("Solr error while attempting to save search", e);
    } catch (IOException e) {
      LOGGER.debug("IOException while attempting to save search", e);
    }
  }

  public void deleteSavedSearch(String uuid) {
    try {
      client.deleteById(ViewerConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, uuid);
      client.commit(ViewerConstants.SOLR_INDEX_SEARCHES_COLLECTION_NAME, true, true);
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

  private void insertDocument(String collection, SolrInputDocument doc) throws ViewerException {
    if (doc == null) {
      throw new ViewerException("Attempted to insert null document into collection " + collection);
    }

    if (collection.equals(ViewerConstants.SOLR_INDEX_DATABASES_COLLECTION_NAME)) {
      insertDocumentNow(collection, doc);
    } else {
      // add document to buffer
      if (!docsByCollection.containsKey(collection)) {
        docsByCollection.put(collection, new ConcurrentLinkedQueue<>());
      }
      Queue<SolrInputDocument> docs = docsByCollection.get(collection);
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

  /**
   * The collections are not immediately available after creation, this method
   * makes sequential attempts to insert documents (previously stored in a
   * buffer) before giving up (by timeout)
   *
   * @throws ViewerException
   *           in case of a fatal error
   */
  private synchronized void insertPendingDocuments() throws ViewerException {
    long timeoutStart = System.currentTimeMillis();
    int tries = 0;
    boolean insertedAllDocuments;
    do {
      UpdateResponse response = null;
      try {
        // add documents, because the buffer limits were reached
        for (String currentCollection : docsByCollection.keySet()) {
          Queue<SolrInputDocument> docs = docsByCollection.get(currentCollection);
          if (docs != null && !docs.isEmpty()) {
            List<SolrInputDocument> batch = new ArrayList<>(docs);
            try {
              response = client.add(currentCollection, batch);
              if (response.getStatus() == 0) {
                collectionsToCommit.add(currentCollection);
                docs.removeAll(batch);

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
                  + ". Last response (if any): " + response, e);
              }
            }
          }
        }
      } catch (SolrServerException | SolrException | IOException e) {
        throw new ViewerException("Problem adding information", e);
      }

      // check if something still needs to be inserted
      insertedAllDocuments = true;
      for (Queue<SolrInputDocument> docs : docsByCollection.values()) {
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
      docsByCollection.entrySet()
        .removeIf(key -> docsByCollection.get(key) == null || docsByCollection.get(key).isEmpty());

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
    updateDatabaseFields(viewerDatabase.getUUID(),
      new Pair<>(ViewerConstants.SOLR_DATABASES_STATUS, ViewerDatabase.Status.AVAILABLE.toString()));
  }

  private void updateDatabaseFields(String databaseUUID, Pair<String, Object>... fields) {
    // create document to update this DB
    SolrInputDocument doc = new SolrInputDocument();
    doc.addField(ViewerConstants.INDEX_ID, databaseUUID);

    // add all the fields that will be updated
    for (Pair<String, Object> field : fields) {
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

  public void updateDatabaseTotalSchemas(String databaseUUID, int totalSchemas) {
    updateDatabaseFields(databaseUUID, new Pair<>(ViewerConstants.SOLR_DATABASES_TOTAL_SCHEMAS, totalSchemas));
  }

  public void updateDatabaseCurrentSchema(String databaseUUID, String schemaName, long completedSchemas,
    int totalTables) {
    updateDatabaseFields(databaseUUID, new Pair<>(ViewerConstants.SOLR_DATABASES_CURRENT_SCHEMA_NAME, schemaName),
      new Pair<>(ViewerConstants.SOLR_DATABASES_INGESTED_SCHEMAS, completedSchemas),
      new Pair<>(ViewerConstants.SOLR_DATABASES_TOTAL_TABLES, totalTables));
  }

  public void updateDatabaseCurrentTable(String databaseUUID, String tableName, long completedTablesInSchema,
    long totalRows) {
    updateDatabaseFields(databaseUUID, new Pair<>(ViewerConstants.SOLR_DATABASES_CURRENT_TABLE_NAME, tableName),
      new Pair<>(ViewerConstants.SOLR_DATABASES_INGESTED_TABLES, completedTablesInSchema),
      new Pair<>(ViewerConstants.SOLR_DATABASES_TOTAL_ROWS, totalRows),
      new Pair<>(ViewerConstants.SOLR_DATABASES_INGESTED_ROWS, 0));
  }

  public void updateDatabaseCurrentRow(String databaseUUID, long completedRows) {
    updateDatabaseFields(databaseUUID, new Pair<>(ViewerConstants.SOLR_DATABASES_INGESTED_ROWS, completedRows));
  }

  public void updateDatabaseIngestionFinished(String databaseUUID) {
    updateDatabaseFields(databaseUUID, new Pair<>(ViewerConstants.SOLR_DATABASES_TOTAL_SCHEMAS, null),
      new Pair<>(ViewerConstants.SOLR_DATABASES_CURRENT_SCHEMA_NAME, null),
      new Pair<>(ViewerConstants.SOLR_DATABASES_INGESTED_SCHEMAS, null),
      new Pair<>(ViewerConstants.SOLR_DATABASES_TOTAL_TABLES, null),
      new Pair<>(ViewerConstants.SOLR_DATABASES_CURRENT_TABLE_NAME, null),
      new Pair<>(ViewerConstants.SOLR_DATABASES_INGESTED_TABLES, null),
      new Pair<>(ViewerConstants.SOLR_DATABASES_TOTAL_ROWS, null),
      new Pair<>(ViewerConstants.SOLR_DATABASES_INGESTED_ROWS, null));
  }
}
