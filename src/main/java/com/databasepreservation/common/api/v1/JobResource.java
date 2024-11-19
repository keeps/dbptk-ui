/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1;

import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.exceptions.RequestNotValidException;
import org.roda.core.data.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.databasepreservation.common.api.exceptions.RESTException;
import com.databasepreservation.common.exceptions.AuthorizationException;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.models.activity.logs.LogEntryState;
import com.databasepreservation.common.client.models.structure.ViewerJob;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.JobService;
import com.databasepreservation.common.server.ViewerFactory;
import com.databasepreservation.common.utils.ControllerAssistant;
import com.databasepreservation.common.utils.I18nUtility;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@RestController
@RequestMapping(path = ViewerConstants.ENDPOINT_JOB)
public class JobResource implements JobService {
  @Autowired
  private HttpServletRequest request;

  @Override
  public IndexResult<ViewerJob> find(FindRequest findRequest, String locale) {
    ControllerAssistant controllerAssistant = new ControllerAssistant() {};

    LogEntryState state = LogEntryState.SUCCESS;
    User user = new User();

    try {
      user = controllerAssistant.checkRoles(request);
      final IndexResult<ViewerJob> result = ViewerFactory.getSolrManager().find(ViewerJob.class, findRequest.filter,
        findRequest.sorter, findRequest.sublist, findRequest.facets);
      return I18nUtility.translate(result, ViewerJob.class, locale);
    } catch (GenericException | RequestNotValidException | AuthorizationException e) {
      state = LogEntryState.FAILURE;
      throw new RESTException(e);
    } finally {
      // register action
      controllerAssistant.registerAction(user, state, ViewerConstants.CONTROLLER_FILTER_PARAM,
        JsonUtils.getJsonFromObject(findRequest.filter));
    }
  }
}
