package com.databasepreservation.common.client.models.wizard.table;

import java.util.List;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ViewAndColumnsParameter extends TableAndColumnsParameter {

  private boolean materialize;

  public ViewAndColumnsParameter() {
    super();
    materialize = false;
  }

  public ViewAndColumnsParameter(String schemaName, String tableName, List<ColumnParameter> columns,
    boolean materialize) {
    super(schemaName, tableName, columns);
    this.materialize = materialize;
  }

  public boolean isMaterialize() {
    return materialize;
  }

  public void setMaterialize(boolean materialize) {
    this.materialize = materialize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;
    ViewAndColumnsParameter that = (ViewAndColumnsParameter) o;
    return isMaterialize() == that.isMaterialize();
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), isMaterialize());
  }
}
