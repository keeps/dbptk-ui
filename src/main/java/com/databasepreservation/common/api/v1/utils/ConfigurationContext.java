/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1.utils;

import java.io.Serial;
import java.io.Serializable;

import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ConfigurationContext implements Serializable {
  @Serial
  private static final long serialVersionUID = 2383367247420214152L;

  private CollectionStatus collectionStatus;
  private ViewerDatabase projectedDatabase;

  public ConfigurationContext() {
  }

  public ConfigurationContext(CollectionStatus collectionStatus, ViewerDatabase projectedDatabase) {
    this.collectionStatus = collectionStatus;
    this.projectedDatabase = projectedDatabase;
  }

  public CollectionStatus getCollectionStatus() {
    return collectionStatus;
  }

  public void setCollectionStatus(CollectionStatus collectionStatus) {
    this.collectionStatus = collectionStatus;
  }

  public ViewerDatabase getProjectedDatabase() {
    return projectedDatabase;
  }

  public void setProjectedDatabase(ViewerDatabase projectedDatabase) {
    this.projectedDatabase = projectedDatabase;
  }
}
