package com.databasepreservation.common.client.models.denormalize;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

public class DenormalizeListConfiguration {
  private List<DenormalizeConfiguration> denormalizeList;

  @JsonProperty("denormalize")
  public List<DenormalizeConfiguration> getDenormalizeList() {
    return denormalizeList;
  }

  public void setDenormalizeList(List<DenormalizeConfiguration> denormalizeList) {
    this.denormalizeList = denormalizeList;
  }
}
