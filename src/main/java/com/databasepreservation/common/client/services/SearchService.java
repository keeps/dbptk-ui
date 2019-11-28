package com.databasepreservation.common.client.services;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.common.search.SavedSearch;
import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.common.search.SearchInfo;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.google.gwt.core.client.GWT;
import io.swagger.annotations.ApiParam;
import org.fusesource.restygwt.client.DirectRestService;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path(".." + ViewerConstants.ENDPOINT_SEARCH)
public interface SearchService extends DirectRestService {

  class Util {
    /**
     * @return the singleton instance
     */
    public static SearchService get() {
      return GWT.create(SearchService.class);
    }

    public static <T> SearchService call(MethodCallback<T> callback) {
      return REST.withCallback(callback).call(get());
    }

    public static <T> SearchService call(Consumer<T> callback) {
      return REST.withCallback(DefaultMethodCallback.get(callback)).call(get());
    }

    public static <T> SearchService call(Consumer<T> callback, Consumer<String> errorHandler) {
      return REST.withCallback(DefaultMethodCallback.get(callback, errorHandler)).call(get());
    }
  }

  @POST
  @Path("save/{databaseUUID}")
  @Produces(MediaType.TEXT_PLAIN)
  String saveSearch(@PathParam("databaseUUID") String databaseUUID, @QueryParam("tableUUID") String tableUUID,
    @QueryParam("tableName") String tableName, @QueryParam("name") String name,
    @QueryParam("description") String description, SearchInfo searchInfo);

  @POST
  @Path("find/{databaseUUID}")
  IndexResult<SavedSearch> findSavedSearches(@PathParam("databaseUUID") String databaseUUID,
    @ApiParam(ViewerConstants.API_QUERY_PARAM_FILTER) FindRequest findRequest,
    @QueryParam(ViewerConstants.API_QUERY_PARAM_LOCALE) String localeString);

  @POST
  @Path("find/{databaseUUID}/{savedSearchUUID}")
  SavedSearch retrieveSavedSearch(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("savedSearchUUID") String savedSearchUUID);

  @POST
  @Path("edit/{databaseUUID}/{savedSearchUUID}")
  void editSearch(@PathParam("databaseUUID") String databaseUUID,
    @PathParam("savedSearchUUID") String savedSearchUUID, @QueryParam("name") String name,
    @QueryParam("description") String description);

  @DELETE
  @Path("delete/{databaseUUID}/{savedSearchUUID}")
  void deleteSearch(@PathParam("databaseUUID") String databaseUUID,
                  @PathParam("savedSearchUUID") String savedSearchUUID);

  @POST
  @Path("find/searchFields")
  List<SearchField> getSearchFields(ViewerTable viewerTable);
}