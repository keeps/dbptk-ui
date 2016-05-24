package com.databasepreservation.dbviewer.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.user.RodaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.dbviewer.ViewerConstants;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerRow;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.exceptions.ViewerException;
import com.databasepreservation.dbviewer.transformers.SolrTransformer;

/**
 * Exposes some methods to interact with a Solr Server connected through HTTP
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SolrManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(SolrManager.class);
  private static final long INSERT_DOCUMENT_TIMEOUT = 30000; // 30 seconds
  private static final int MAX_CACHED_DOCUMENTS_PER_COLLECTION = 10;
  private static final int MAX_CACHED_COLLECTIONS = 10;

  private final HttpSolrClient client;
  private final Set<String> collectionsToCommit;
  private Map<String, List<SolrInputDocument>> docsByCollection = new HashMap<>();
  private boolean setupDone = false;

  public SolrManager(String url) {
    client = new HttpSolrClient(url);
    client.setConnectionTimeout(5000);
    // allowCompression defaults to false.
    // Server side must support gzip or deflate for this to have any effect.
    // solrDBList.setAllowCompression(true);

    // TODO: ensure that solr is running in cloud mode before execution

    collectionsToCommit = new HashSet<>();
  }

  /**
   * Adds a database to the databases collection
   * 
   * @param database
   *          the new database
   */
  public void addDatabase(ViewerDatabase database) throws ViewerException {
    // creates databases collection, skipping if it is present
    CollectionAdminRequest.Create request = new CollectionAdminRequest.Create();
    request.setCollectionName(ViewerConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME);
    request.setConfigName(ViewerConstants.SOLR_CONFIGSET_DATABASE);
    request.setNumShards(1);
    try {
      NamedList<Object> response = client.request(request);
    } catch (SolrServerException | IOException e) {
      throw new ViewerException("Error creating collection " + ViewerConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME, e);
    } catch (HttpSolrClient.RemoteSolrException e) {
      if (e.getMessage().contains("collection already exists: " + ViewerConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME)) {
        LOGGER.info("collection " + ViewerConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME + " already exists.");
      } else {
        throw new ViewerException("Error creating collection " + ViewerConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME, e);
      }
    }

    // add this database to the collection
    collectionsToCommit.add(ViewerConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME);
    insertDocument(ViewerConstants.SOLR_INDEX_DATABASE_COLLECTION_NAME, SolrTransformer.fromDatabase(database));

    // prepare tables to create the collection
    final List<ViewerTable> tables = new ArrayList<>();
    for (ViewerSchema viewerSchema : database.getMetadata().getSchemas()) {
      for (ViewerTable viewerTable : viewerSchema.getTables()) {
        tables.add(viewerTable);
      }
    }

    Thread tableCollectionCreator = new Thread(){
      public void run(){
        for (ViewerTable table : tables) {
          String collectionName = SolrUtils.getTableCollectionName(table.getUUID());
          CollectionAdminRequest.Create request = new CollectionAdminRequest.Create();
          request.setCollectionName(collectionName);
          request.setConfigName(ViewerConstants.SOLR_CONFIGSET_TABLE);
          request.setNumShards(1);

          try {
            LOGGER.debug("Creating collection for table " + table.getName() + " with id " + table.getUUID());
            NamedList<Object> response = client.request(request);
            LOGGER.debug("Response from server (create collection for table with id " + table.getUUID() + "): "
              + response.toString());
          } catch (SolrServerException | IOException e) {
            LOGGER.error("Error creating collection " + collectionName, e);
          }
        }
      }
    };

    tableCollectionCreator.start();
  }

  /**
   * Creates a new table collection in solr for the specified table
   *
   * @param table
   *          the table which data is going to be saved in this collections
   */
  public void addTable(ViewerTable table) throws ViewerException {
    String collectionName = SolrUtils.getTableCollectionName(table.getUUID());
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

  public <T extends IsIndexed> IndexResult<T> find(RodaUser user, Class<T> classToReturn, Filter filter, Sorter sorter,
    Sublist sublist, Facets facets) throws org.roda.core.data.exceptions.GenericException, RequestNotValidException {
    return SolrUtils.find(client, classToReturn, filter, sorter, sublist, facets);
  }

  public <T extends IsIndexed> Long count(RodaUser user, Class<T> classToReturn, Filter filter)
    throws org.roda.core.data.exceptions.GenericException, RequestNotValidException {
    return SolrUtils.count(client, classToReturn, filter);
  }

  public <T extends IsIndexed> T retrieve(RodaUser user, Class<T> classToReturn, String id) throws NotFoundException,
    org.roda.core.data.exceptions.GenericException {
    return SolrUtils.retrieve(client, classToReturn, id);
  }

  public <T extends IsIndexed> IndexResult<T> findRows(RodaUser user, Class<T> classToReturn, String tableUUID,
    Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws org.roda.core.data.exceptions.GenericException, RequestNotValidException {
    return SolrUtils.find(client, classToReturn, tableUUID, filter, sorter, sublist, facets);
  }

  public <T extends IsIndexed> Long countRows(RodaUser user, Class<T> classToReturn, String tableUUID, Filter filter)
    throws org.roda.core.data.exceptions.GenericException, RequestNotValidException {
    return SolrUtils.count(client, classToReturn, tableUUID, filter);
  }

  public <T extends IsIndexed> T retrieveRows(RodaUser user, Class<T> classToReturn, String tableUUID, String rowUUID)
    throws NotFoundException, org.roda.core.data.exceptions.GenericException {
    return SolrUtils.retrieve(client, classToReturn, tableUUID, rowUUID);
  }

  private void insertDocument(String collection, SolrInputDocument doc) throws ViewerException {
    if (doc == null) {
      throw new ViewerException("Attempted to insert null document into collection " + collection);
    }
    insertDocumentOrCommitAndOptimize(collection, doc);
  }

  private void commitAndOptimize(String collection) throws ViewerException {
    insertDocumentOrCommitAndOptimize(collection, null);
  }

  /**
   * The collections are not immediately available after creation, this method
   * makes sequential attempts to insert a document before giving up (by
   * timeout)
   *
   * Do not use this method, use insertDocument and commitAndOptimize instead.
   *
   * @param collection
   *          the collection to insert the document or commit and optimize
   * @param doc
   *          the document to insert (to commit and optimize, this must be null)
   * @throws ViewerException
   */
  private void insertDocumentOrCommitAndOptimize(String collection, SolrInputDocument doc) throws ViewerException {
    if (doc != null) {
      if (!docsByCollection.containsKey(collection)) {
        docsByCollection.put(collection, new ArrayList<SolrInputDocument>());
      }
      List<SolrInputDocument> docs = docsByCollection.get(collection);
      docs.add(doc);
      if (docs.size() < MAX_CACHED_DOCUMENTS_PER_COLLECTION && docsByCollection.size() < MAX_CACHED_COLLECTIONS) {
        return;
      }
    }

    long start = System.currentTimeMillis();
    int tries = 0;
    while (System.currentTimeMillis() - start < INSERT_DOCUMENT_TIMEOUT) {
      UpdateResponse response;
      try {
        if (docsByCollection.size() > 0) {
          // add documents, either because there is going to be a commit or the
          // cache limits were reached
          try {

            for (Map.Entry<String, List<SolrInputDocument>> collectionAndDocuments : docsByCollection.entrySet()) {
              List<SolrInputDocument> docs = collectionAndDocuments.getValue();
              if (!docs.isEmpty()) {
                String currentCollection = collectionAndDocuments.getKey();
                response = client.add(currentCollection, docs);
                if (response.getStatus() == 0) {
                  docsByCollection.remove(currentCollection);
                  return;
                } else {
                  throw new ViewerException("Could not insert document {" + doc + "} in collection " + collection);
                }
              }
            }
          } catch (SolrServerException | IOException e) {
            throw new ViewerException("Problem inserting document", e);
          }
        }

        if (doc == null) {
          // commit and optimize
          try {
            response = client.commit(collection);
            if (response.getStatus() != 0) {
              throw new ViewerException("Could not commit collection " + collection);
            }
          } catch (SolrServerException | IOException e) {
            throw new ViewerException("Problem committing collection " + collection, e);
          }

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
        }
      } catch (HttpSolrClient.RemoteSolrException e) {
        if (e.getMessage().contains("<title>Error 404 Not Found</title>")) {
          // this means that the collection does not exist yet. retry
          LOGGER.debug("Collection " + collection + " does not exist. Retrying (" + tries + ")");
        } else {
          throw e;
        }
      }

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        LOGGER.debug("insertDocument sleep was interrupted", e);
      }
      tries++;
    }

    if (doc != null) {
      throw new ViewerException("insertDocument {" + doc + "} in collection " + collection + " timed out after "
        + tries + " attempts.");
    } else {
      throw new ViewerException("commit and optimize collection " + collection + " timed out after " + tries
        + " attempts.");
    }
  }
}
