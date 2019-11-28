package com.databasepreservation.common.client.models.parameters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MetadataExportOptionsParameters implements Serializable {

  private Map<String, String> values = new HashMap<>();

  public MetadataExportOptionsParameters() {
  }

  public MetadataExportOptionsParameters(Map<String, String> values) {
    this.values = values;
  }

  public Map<String, String> getValues() {
    return values;
  }

  public void setValues(Map<String, String> values) {
    this.values = values;
  }
}
