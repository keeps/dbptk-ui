package com.databasepreservation.common.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.exceptions.RESTException;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.JobService;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.I18nUtility;
import com.databasepreservation.common.utils.UserUtility;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_JOB)
public class JobResource implements JobService {
  @Context
  private HttpServletRequest request;

  @Override
  public IndexResult<ViewerJob> find(FindRequest findRequest, String locale) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = controllerAssistant.checkRoles(request);

    try {
      final IndexResult<ViewerJob> result = ViewerFactory.getSolrManager().find(ViewerJob.class, findRequest.filter,
        findRequest.sorter, findRequest.sublist, findRequest.facets);
      return I18nUtility.translate(result, ViewerJob.class, locale);
    } catch (GenericException | RequestNotValidException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILTER_PARAM,
        JsonUtils.getJsonFromObject(findRequest.filter));
    }
  }
}
