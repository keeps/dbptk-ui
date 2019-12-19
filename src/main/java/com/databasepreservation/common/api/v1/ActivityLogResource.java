package com.databasepreservation.common.api.v1;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.core.data.v2.index.sublist.Sublist;
import org.roda.core.data.v2.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.models.activity.logs.LogEntryParameter;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.services.ActivityLogService;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.I18nUtility;
import com.databasepreservation.common.utils.UserUtility;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_ACTIVITY_LOG)
public class ActivityLogResource implements ActivityLogService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ActivityLogResource.class);

  @Context
  private HttpServletRequest request;

  @Override
  public IndexResult<ActivityLogEntry> find(FindRequest findRequest, String locale) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      final IndexResult<ActivityLogEntry> result = ViewerFactory.getSolrManager().find(ActivityLogEntry.class, findRequest.filter, findRequest.sorter,
          findRequest.sublist, findRequest.facets);
      return I18nUtility.translate(result, ActivityLogEntry.class, locale);
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILTER_PARAM,
        JsonUtils.getJsonFromObject(findRequest.filter));
    }
  }

  @Override
  public ActivityLogEntry retrieve(String logUUID) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};
    User user = UserUtility.getUser(request);
    LogEntryState state = LogEntryState.SUCCESS;

    controllerAssistant.checkRoles(user);

    try {
      final ActivityLogEntry retrieve = ViewerFactory.getSolrManager().retrieve(ActivityLogEntry.class, logUUID);

      final ViewerDatabase viewerDatabase = getViewerDatabase(retrieve.getParameters());
      if (viewerDatabase != null) {
        final Map<String, String> displayNameColumn = getDisplayNameColumn(viewerDatabase.getMetadata(),
          retrieve.getParameters());
        final List<LogEntryParameter> parameters = replaceColumnSolrName(displayNameColumn, retrieve.getParameters());

        retrieve.setParameters(parameters);
        return retrieve;
      }

      return null;
    } catch (GenericException | NotFoundException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_LOG_ID_PARAM, logUUID);
    }
  }

  private ViewerDatabase getViewerDatabase(List<LogEntryParameter> parameters)
    throws GenericException, RequestNotValidException {
    for (LogEntryParameter parameter : parameters) {
      if (parameter.getName().equals(ViewerConstants.CONTROLLER_DATABASE_ID_PARAM)) {
        final String databaseUuid = parameter.getValue();
        List<String> fieldsToReturn = Collections.singletonList(ViewerConstants.SOLR_DATABASES_METADATA);
        Filter filterParam = new Filter(new SimpleFilterParameter(ViewerConstants.INDEX_ID, databaseUuid));
        final IndexResult<ViewerDatabase> viewerDatabase = ViewerFactory.getSolrManager().find(ViewerDatabase.class,
          filterParam, Sorter.NONE, new Sublist(), Facets.NONE, fieldsToReturn);

        return viewerDatabase.getResults().get(0);
      }
    }

    return null;
  }

  private List<LogEntryParameter> replaceColumnSolrName(Map<String, String> mapperSolrToDisplayName,
    List<LogEntryParameter> parameters) throws GenericException {

    for (LogEntryParameter parameter : parameters) {
      if (parameter.getName().equals(ViewerConstants.CONTROLLER_FILTER_PARAM)) {
        final Filter filter = JsonUtils.getObjectFromJson(parameter.getValue(), Filter.class);
        for (FilterParameter filterParameter : filter.getParameters()) {
          if (filterParameter.getName().startsWith(ViewerConstants.SOLR_INDEX_ROW_COLUMN_NAME_PREFIX)) {
            filterParameter.setName(mapperSolrToDisplayName.get(filterParameter.getName()));
            final String jsonFromObject = JsonUtils.getJsonFromObject(filter);
            parameter.setValue(jsonFromObject);
          }
        }
      }
    }

    return parameters;
  }

  private String getTableIdFromFilter(List<LogEntryParameter> parameters) throws GenericException {
    for (LogEntryParameter parameter : parameters) {
      if (parameter.getName().equals(ViewerConstants.CONTROLLER_FILTER_PARAM)) {
        final Filter filter = JsonUtils.getObjectFromJson(parameter.getValue(), Filter.class);
        for (FilterParameter filterParameter : filter.getParameters()) {
          if (filterParameter.getName().equals(ViewerConstants.SOLR_ROWS_TABLE_ID)
            && filterParameter instanceof SimpleFilterParameter) {
            return ((SimpleFilterParameter) filterParameter).getValue();
          }
        }
      }
    }

    return null;
  }

  private Map<String, String> getDisplayNameColumn(ViewerMetadata metadata, List<LogEntryParameter> parameters)
    throws GenericException {
    Map<String, String> solrNameToDisplayName = new HashMap<>();

    String tableId = getTableIdFromFilter(parameters);

    for (LogEntryParameter parameter : parameters) {
      if (parameter.getName().equals(ViewerConstants.CONTROLLER_FILTER_PARAM)) {
        final Filter filter = JsonUtils.getObjectFromJson(parameter.getValue(), Filter.class);
        for (FilterParameter filterParameter : filter.getParameters()) {
          if (filterParameter.getName().startsWith(ViewerConstants.SOLR_INDEX_ROW_COLUMN_NAME_PREFIX)) {
            final List<ViewerColumn> columns = metadata.getTableById(tableId).getColumns();

            for (ViewerColumn column : columns) {
              if (column.getSolrName().equals(filterParameter.getName())) {
                solrNameToDisplayName.put(column.getSolrName(), column.getDisplayName());
              }
            }
          }
        }
      }
    }
    return solrNameToDisplayName;
  }
}
