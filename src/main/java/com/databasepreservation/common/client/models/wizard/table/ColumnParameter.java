/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.wizard.table;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ColumnParameter implements Serializable {

  private String name;
  private ExternalLobParameter externalLobParameter;
  private boolean useOnMerkle;

  public ColumnParameter() {
    useOnMerkle = true;
  }

  public ColumnParameter(ExternalLobParameter externalLobParameter, String name, boolean useOnMerkle) {
    this.externalLobParameter = externalLobParameter;
    this.name = name;
    this.useOnMerkle = useOnMerkle;
  }

  public ExternalLobParameter getExternalLobParameter() {
    return externalLobParameter;
  }

  public void setExternalLobParameter(ExternalLobParameter externalLobParameter) {
    this.externalLobParameter = externalLobParameter;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isUseOnMerkle() {
    return useOnMerkle;
  }

  public void setUseOnMerkle(boolean useOnMerkle) {
    this.useOnMerkle = useOnMerkle;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ColumnParameter that = (ColumnParameter) o;
    return isUseOnMerkle() == that.isUseOnMerkle()
      && Objects.equals(getExternalLobParameter(), that.getExternalLobParameter()) && getName().equals(that.getName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getExternalLobParameter(), getName(), isUseOnMerkle());
  }
}
