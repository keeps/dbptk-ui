/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.activity.log.operations;

import com.databasepreservation.common.client.models.activity.logs.PresenceState;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class FacetsOperation implements Operation {
  private static final Logger LOGGER = LoggerFactory.getLogger(FacetsOperation.class);

  @Override
  public ActivityLogWrapper execute(ActivityLogWrapper wrapper) {
    try {
      final String jsonFacets = wrapper.getActivityLogEntry().getParameters().get(ViewerConstants.CONTROLLER_FACET_PARAM);
      Facets facets;
      if (jsonFacets != null) {
        wrapper.setFacetsPresence(PresenceState.YES);
        facets = JsonUtils.getObjectFromJson(jsonFacets, Facets.class);
      } else {
        wrapper.setFacetsPresence(PresenceState.NO);
        facets = Facets.NONE;
      }

      wrapper.setFacets(facets);
    } catch (GenericException e) {
      LOGGER.debug("Error executing the facets operation", e);
    }

    return wrapper;
  }
}
