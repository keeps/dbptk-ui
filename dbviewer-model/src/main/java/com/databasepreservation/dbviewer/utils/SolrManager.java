package com.databasepreservation.dbviewer.utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
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

  private final HttpSolrClient client;
  private final Set<String> collectionsToCommit;
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
    request.setCollectionName(ViewerConstants.SOLR_INDEX_DATABASE);
    request.setConfigName(ViewerConstants.SOLR_CONFIGSET_DATABASE);
    request.setNumShards(1);
    try {
      NamedList<Object> response = client.request(request);
    } catch (SolrServerException | IOException e) {
      throw new ViewerException("Error creating collection " + ViewerConstants.SOLR_INDEX_DATABASE, e);
    } catch (HttpSolrClient.RemoteSolrException e) {
      if (e.getMessage().contains("collection already exists: " + ViewerConstants.SOLR_INDEX_DATABASE)) {
        LOGGER.info("collection " + ViewerConstants.SOLR_INDEX_DATABASE + " already exists.");
      } else {
        throw new ViewerException("Error creating collection " + ViewerConstants.SOLR_INDEX_DATABASE, e);
      }
    }

    // add this database to the collection
    collectionsToCommit.add(ViewerConstants.SOLR_INDEX_DATABASE);
    try {
      client.add(ViewerConstants.SOLR_INDEX_DATABASE, SolrTransformer.fromDatabase(database));
    } catch (SolrServerException | IOException e) {
      throw new ViewerException("Error adding database", e);
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
      throw new ViewerException("Error creating collection " + collectionName, e);
    }
    collectionsToCommit.add(collectionName);
  }

  public void addRow(ViewerTable table, ViewerRow row) throws ViewerException {
    String collectionName = SolrUtils.getTableCollectionName(table.getUUID());
    try {
      client.add(collectionName, SolrTransformer.fromRow(table, row));
    } catch (SolrServerException | IOException e) {
      throw new ViewerException("Error adding row", e);
    }
  }

  /**
   * Commits all changes to all modified collections and optimizes them
   * 
   * @throws ViewerException
   */
  public void commitAll() throws ViewerException {
    // TODO: replace with better solution
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    try {
      for (String collection : collectionsToCommit) {
        client.commit(collection);
      }
    } catch (IOException | SolrServerException e) {
      throw new ViewerException("Error committing collection", e);
    }

    try {
      for (String collection : collectionsToCommit) {
        client.optimize(collection);
      }
    } catch (IOException | SolrServerException e) {
      throw new ViewerException("Error optimizing collection", e);
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
}
