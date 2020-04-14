package com.databasepreservation.common.client.models.wizard.table;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExternalLobParameter implements Serializable {

  private String referenceType;
  private String basePath;

  public ExternalLobParameter() {
  }

  public ExternalLobParameter(String referenceType, String basePath) {
    this.referenceType = referenceType;
    this.basePath = basePath;
  }

  public String getReferenceType() {
    return referenceType;
  }

  public void setReferenceType(String referenceType) {
    this.referenceType = referenceType;
  }

  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ExternalLobParameter that = (ExternalLobParameter) o;
    return Objects.equals(getReferenceType(), that.getReferenceType())
      && Objects.equals(getBasePath(), that.getBasePath());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getReferenceType(), getBasePath());
  }
}
