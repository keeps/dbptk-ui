package com.databasepreservation.main.common.shared.models.wizardParameters;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExportOptionsParameters implements Serializable {

  private String SIARDVersion = null;
  private String siardPath = null;
  private HashMap<String, String> parameters = new HashMap<>();

  public ExportOptionsParameters() {
  }

  public ExportOptionsParameters(String SIARDVersion, String siardPath, HashMap<String, String> parameters) {
    this.SIARDVersion = SIARDVersion;
    this.siardPath = siardPath;
    this.parameters = parameters;
  }

  public String getSIARDVersion() {
    return SIARDVersion;
  }

  public void setSIARDVersion(String SIARDVersion) {
    this.SIARDVersion = SIARDVersion;
  }

  public String getSiardPath() {
    return siardPath;
  }

  public void setSiardPath(String siardPath) {
    this.siardPath = siardPath;
  }

  public HashMap<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(HashMap<String, String> parameters) {
    this.parameters = parameters;
  }
}
