package com.databasepreservation.common.client.tools;

import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseValidationStatus;
import com.google.gwt.core.client.GWT;
import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SolrHumanizer {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static String humanize(ViewerDatabaseStatus status) {
    switch (status) {
      case INGESTING:
        return messages.humanizedTextForSolrIngesting();
      case AVAILABLE:
        return messages.humanizedTextForSolrAvailable();
      case METADATA_ONLY:
        return messages.humanizedTextForSolrMetadataOnly();
      case REMOVING:
        return messages.humanizedTextForSolrRemoving();
      case ERROR:
        return messages.humanizedTextForSolrError();
      default:
        return "";
    }
  }

  public static String humanize(ViewerDatabaseValidationStatus status) {
    switch (status) {
      case NOT_VALIDATED:
        return messages.humanizedTextForSIARDNotValidated();
      case VALIDATION_SUCCESS:
        return messages.humanizedTextForSIARDValidationSuccess();
      case VALIDATION_FAILED:
        return messages.humanizedTextForSIARDValidationFailed();
      case VALIDATION_RUNNING:
        return messages.humanizedTextForSIARDValidationRunning();
      case ERROR:
        return messages.alertErrorTitle();
      default:
        return "";
    }
  }
}
