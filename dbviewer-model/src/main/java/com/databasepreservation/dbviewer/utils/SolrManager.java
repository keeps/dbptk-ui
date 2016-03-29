package com.databasepreservation.dbviewer.utils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.roda.core.data.adapter.facet.Facets;
import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.adapter.sort.Sorter;
import org.roda.core.data.adapter.sublist.Sublist;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.user.RodaUser;

import com.databasepreservation.dbviewer.ViewerConstants;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.exceptions.ViewerException;
import com.databasepreservation.dbviewer.transformers.SolrTransformer;

/**
 * Exposes some methods to interact with a Solr Server connected through HTTP
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SolrManager {
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
    } catch (SolrServerException e) {
      throw new ViewerException(e);
    } catch (IOException e) {
      throw new ViewerException(e);
    }
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
    } catch (IOException e) {
      throw new ViewerException("Error committing collection", e);
    } catch (SolrServerException e) {
      throw new ViewerException("Error committing collection", e);
    }

    try {
      for (String collection : collectionsToCommit) {
        client.optimize(collection);
      }
    } catch (IOException e) {
      throw new ViewerException("Error optimizing collection", e);
    } catch (SolrServerException e) {
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
