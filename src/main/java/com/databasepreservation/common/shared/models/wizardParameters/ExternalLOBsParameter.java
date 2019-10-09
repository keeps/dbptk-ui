package com.databasepreservation.common.shared.models.wizardParameters;

import java.io.Serializable;
import java.util.Objects;

import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExternalLOBsParameter implements Serializable {

  private ViewerTable table;
  private String columnName;
  private String referenceType;
  private String basePath;

  public ExternalLOBsParameter() {
  }

  public ExternalLOBsParameter(ViewerTable table, String columnName, String referenceType, String basePath) {
    this.table = table;
    this.columnName = columnName;
    this.referenceType = referenceType;
    this.basePath = basePath;
  }

  public ViewerTable getTable() {
    return table;
  }

  public String getColumnName() {
    return columnName;
  }

  public String getReferenceType() {
    return referenceType;
  }

  public String getBasePath() {
    return basePath;
  }

  public void setTable(ViewerTable table) {
    this.table = table;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public void setReferenceType(String referenceType) {
    this.referenceType = referenceType;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExternalLOBsParameter that = (ExternalLOBsParameter) o;
    return Objects.equals(getTable(), that.getTable()) &&
        Objects.equals(getColumnName(), that.getColumnName()) &&
        Objects.equals(getReferenceType(), that.getReferenceType()) &&
        Objects.equals(getBasePath(), that.getBasePath());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTable(), getColumnName(), getReferenceType(), getBasePath());
  }
}
