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
import org.roda.core.data.v2.index.sublist.Sublist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SublistOperation implements Operation {
  private static final Logger LOGGER = LoggerFactory.getLogger(SublistOperation.class);

  @Override
  public ActivityLogWrapper execute(ActivityLogWrapper wrapper) {
    try {
      final String jsonFilter = wrapper.getActivityLogEntry().getParameters().get(ViewerConstants.CONTROLLER_SUBLIST_PARAM);
      if (jsonFilter != null) {
        final Sublist sublist = JsonUtils.getObjectFromJson(jsonFilter, Sublist.class);
        wrapper.setSublist(sublist);
        wrapper.setSublistPresence(PresenceState.YES);
      }
    } catch (GenericException e) {
      LOGGER.debug("Error executing the sublist information", e);
    }

    return wrapper;
  }
}
