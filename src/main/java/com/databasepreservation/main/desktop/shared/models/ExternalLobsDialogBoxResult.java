package com.databasepreservation.main.desktop.shared.models;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExternalLobsDialogBoxResult implements Serializable {

  private String option;
  private boolean result;

  public ExternalLobsDialogBoxResult() {
  }

  public ExternalLobsDialogBoxResult(String option, boolean result) {
    this.option = option;
    this.result = result;
  }

  public String getOption() {
    return option;
  }

  public void setOption(String option) {
    this.option = option;
  }

  public boolean isResult() {
    return result;
  }

  public void setResult(boolean result) {
    this.result = result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExternalLobsDialogBoxResult that = (ExternalLobsDialogBoxResult) o;
    return isResult() == that.isResult() &&
        Objects.equals(getOption(), that.getOption());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getOption(), isResult());
  }
}
