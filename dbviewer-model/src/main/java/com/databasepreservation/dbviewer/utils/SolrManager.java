package com.databasepreservation.dbviewer.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
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



  public SolrManager(String url) {
    client = new HttpSolrClient(url);
    client.setConnectionTimeout(5000);
    // allowCompression defaults to false.
    // Server side must support gzip or deflate for this to have any effect.
    // solrDBList.setAllowCompression(true);

    collectionsToCommit = new HashSet<>();
  }

  /**
   * Adds a database to the databases collection
   * 
   * @param database
   *          the new database
   */
  public void addDatabase(ViewerDatabase database) throws ViewerException {
    collectionsToCommit.add(ViewerConstants.SOLR_INDEX_DATABASE);
    try {
      client.add(ViewerConstants.SOLR_INDEX_DATABASE, SolrTransformer.fromDatabase(database));
    } catch (SolrServerException | IOException e) {
      throw new ViewerException(e);
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
    request.setNumShards(1);

    XMLResponseParser responseParser = new XMLResponseParser();
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

  private List<String> getCoreListFromSolrResponse(NamedList<Object> response) throws ViewerException {
    Map tree = response.asMap(0);

    // check if response status was OK
    Map responseHeader = (Map) tree.get("responseHeader");
    Integer status = (Integer)responseHeader.get("status");
    if(status != 0){
      throw new ViewerException("Get non-zero status in response: "+response.toString());
    }

    // get core names
    return Arrays.asList("nope");
  }

  /**
   * Commits all changes to all modified collections and optimizes them
   * 
   * @throws ViewerException
   */
  public void commitAll() throws ViewerException {
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
