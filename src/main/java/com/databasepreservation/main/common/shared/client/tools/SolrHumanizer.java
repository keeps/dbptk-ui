package com.databasepreservation.main.common.shared.client.tools;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase.Status;
import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SolrHumanizer {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static String humanize(Status status) {
    switch (status) {
      case INGESTING:
        return messages.solrIngesting();
      case AVAILABLE:
        return messages.solrAvailable();
      case METADATA_ONLY:
        return messages.solrMetadataOnly();
      case REMOVING:
        return messages.solrRemoving();
      case ERROR:
        return messages.solrError();
      default:
        return "";
    }
  }

  public static String humanize(ViewerDatabase.ValidationStatus status) {
    switch (status) {
      case NOT_VALIDATED:
        return messages.SIARDNotValidated();
      case VALIDATION_SUCCESS:
        return messages.SIARDValidationSuccess();
      case VALIDATION_FAILED:
        return messages.SIARDValidationFailed();
      default:
        return "";
    }
  }
}
