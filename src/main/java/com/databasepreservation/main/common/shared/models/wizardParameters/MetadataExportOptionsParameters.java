package com.databasepreservation.main.common.shared.models.wizardParameters;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MetadataExportOptionsParameters implements Serializable {

  private HashMap<String, String> values = new HashMap<>();

  public MetadataExportOptionsParameters() {
  }

  public MetadataExportOptionsParameters(HashMap<String, String> values) {
    this.values = values;
  }

  public HashMap<String, String> getValues() {
    return values;
  }

  public void setValues(HashMap<String, String> values) {
    this.values = values;
  }
}
