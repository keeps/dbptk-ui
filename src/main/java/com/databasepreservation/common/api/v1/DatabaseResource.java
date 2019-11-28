package com.databasepreservation.common.api.v1;

import com.databasepreservation.common.client.models.ProgressData;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerRow;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.parameters.ConnectionParameters;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.server.controller.SIARDController;
import com.databasepreservation.common.server.index.factory.SolrClientFactory;
import com.databasepreservation.common.server.index.schema.SolrDefaultCollectionRegistry;
import com.databasepreservation.common.server.index.utils.SolrUtils;
import com.databasepreservation.common.utils.UserUtility;
import org.roda.core.data.exceptions.AuthorizationDeniedException;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.List;

import static com.databasepreservation.common.client.ViewerConstants.SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX;
import static com.databasepreservation.common.client.ViewerConstants.SOLR_SEARCHES_DATABASE_UUID;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_DATABASE)
public class DatabaseResource implements DatabaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseResource.class);
  @Context
  private HttpServletRequest request;

  @Override
  public String generateUUID() {
    return SolrUtils.randomUUID();
  }

  @Override
  public ViewerMetadata getSchemaInformation(ConnectionParameters connectionParameters) {
    try {
      return SIARDController.getDatabaseMetadata(connectionParameters);
    } catch (GenericException e) {
      throw new RESTException(e);
    }
  }

  @Override
  public List<List<String>> validateCustomViewQuery(ConnectionParameters parameters, String query) {
    try {
      return SIARDController.validateCustomViewQuery(parameters, query);
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public ProgressData getProgressData(String databaseuuid) {
    return ProgressData.getInstance(databaseuuid);
  }

  @Override
  public IndexResult<ViewerDatabase> findDatabases(FindRequest findRequest, String localeString){
    try {
      UserUtility.Authorization.allowIfAdmin(request);
    } catch (AuthorizationDeniedException e) {
      // TODO change to other exception and add exception mapper to return a different response error code
      throw new RESTException(e.getMessage());
    }
    try {
      return ViewerFactory.getSolrManager().find(ViewerDatabase.class, findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets);
    } catch (GenericException | RequestNotValidException e) {
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public ViewerDatabase retrieve(String databaseUUID, String id) {
    try {
      ViewerDatabase result = ViewerFactory.getSolrManager().retrieve(ViewerDatabase.class, id);
      UserUtility.Authorization.checkRetrievalPermission(request, databaseUUID, ViewerDatabase.class, result);
      return result;
    } catch (NotFoundException | GenericException e) {
      throw new RESTException(e.getMessage());
    } catch (AuthorizationDeniedException e) {
      // TODO change to other exception and add exception mapper to return a different response error code
      throw new RESTException(e.getMessage());
    }
  }

  @Override
  public Boolean deleteDatabase(String databaseUUID) {
    return SIARDController.deleteAll(databaseUUID);
  }

  @Override
  public Boolean deleteRows(String databaseUUID) {
    try {
      final String collectionName = SOLR_INDEX_ROW_COLLECTION_NAME_PREFIX + databaseUUID;
      if (SolrClientFactory.get().deleteCollection(collectionName)) {
        Filter savedSearchFilter = new Filter(new SimpleFilterParameter(SOLR_SEARCHES_DATABASE_UUID, databaseUUID));
        SolrUtils.delete(ViewerFactory.getSolrClient(), SolrDefaultCollectionRegistry.get(SavedSearch.class),
            savedSearchFilter);

        ViewerFactory.getSolrManager().markDatabaseCollection(databaseUUID, ViewerDatabaseStatus.METADATA_ONLY);
        return true;
      }
    } catch (GenericException | RequestNotValidException e) {
      LOGGER.error("Error trying to remove the collection from Solr", e);
      return false;
    }
    return false;
  }

  //
  @Override
  public IndexResult<ViewerRow> findRows(String databaseUUID, FindRequest findRequest, String localeString) {
    try {
      UserUtility.Authorization.checkDatabaseAccessPermission(request, databaseUUID);
    } catch (AuthorizationDeniedException | GenericException | NotFoundException e) {
      // TODO change to other exception and add exception mapper to return a different response error code
      throw new RESTException(e.getMessage());
    }
    try {
      return ViewerFactory.getSolrManager().findRows(databaseUUID, findRequest.filter, findRequest.sorter, findRequest.sublist, findRequest.facets);
    } catch (GenericException e) {
      throw new RESTException(e.getMessage());
    } catch (RequestNotValidException e) {
      throw new RESTException("Invalid database UUID: " + databaseUUID);
    }
  }

  @Override
  public ViewerRow retrieveRows(String databaseUUID, String rowUUID) {
    try {
      UserUtility.Authorization.checkDatabaseAccessPermission(request, databaseUUID);
    } catch (AuthorizationDeniedException e) {
      // TODO change to other exception and add exception mapper to return a different response error code
      throw new RESTException(e.getMessage());
    } catch (NotFoundException | GenericException e) {
      throw new RESTException(e.getMessage());
    }
    try {
      return ViewerFactory.getSolrManager().retrieveRows(databaseUUID, rowUUID);
    } catch (NotFoundException | GenericException e) {
      throw new RESTException(e.getMessage());
    }
  }
}
