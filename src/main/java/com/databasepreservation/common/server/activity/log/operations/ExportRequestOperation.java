package com.databasepreservation.common.server.activity.log.operations;

import com.databasepreservation.common.client.models.activity.logs.PresenceState;
import org.roda.core.data.exceptions.GenericException;
import org.roda.core.data.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.ExportRequest;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogWrapper;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExportRequestOperation implements Operation {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExportRequestOperation.class);

  @Override
  public ActivityLogWrapper execute(ActivityLogWrapper wrapper) {
    try {
      final String jsonExport = wrapper.getActivityLogEntry().getParameters().get(ViewerConstants.CONTROLLER_EXPORT_PARAM);
      final ExportRequest exportRequest = JsonUtils.getObjectFromJson(jsonExport, ExportRequest.class);
      wrapper.setExportRequest(exportRequest);
      wrapper.setExportRequestPresence(PresenceState.YES);
    } catch (GenericException e) {
      LOGGER.debug("Error executing the export request information", e);
    }

    return wrapper;
  }
}
