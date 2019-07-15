package com.databasepreservation.main.desktop.shared.models.wizardParameters;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExportOptionsParameters implements Serializable {

  private String SIARDVersion = null;
  private HashMap<String, String> parameters = new HashMap<>();

  public ExportOptionsParameters() {
  }

  public ExportOptionsParameters(String SIARDVersion, HashMap<String, String> parameters) {
    this.SIARDVersion = SIARDVersion;
    this.parameters = parameters;
  }

  public String getSIARDVersion() {
    return SIARDVersion;
  }

  public void setSIARDVersion(String SIARDVersion) {
    this.SIARDVersion = SIARDVersion;
  }

  public HashMap<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(HashMap<String, String> parameters) {
    this.parameters = parameters;
  }
}
