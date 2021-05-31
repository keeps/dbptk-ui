package com.databasepreservation.common.client.models.status.formatters;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonTypeName("noFormatter")
public class NoFormatter implements Formatter {

  public NoFormatter() {
  }
}
