package com.databasepreservation.common.shared.models.wizardParameters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExportOptionsParameters implements Serializable {

  private String siardVersion = null;
  private String siardPath = null;
  private Map<String, String> parameters = new HashMap<>();

  public ExportOptionsParameters() {
  }

  public ExportOptionsParameters(String siardVersion, String siardPath, Map<String, String> parameters) {
    this.siardVersion = siardVersion;
    this.siardPath = siardPath;
    this.parameters = parameters;
  }

  public String getSiardVersion() {
    return siardVersion;
  }

  public void setSiardVersion(String siardVersion) {
    this.siardVersion = siardVersion;
  }

  public String getSiardPath() {
    return siardPath;
  }

  public void setSiardPath(String siardPath) {
    this.siardPath = siardPath;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }
}
