/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.parameters;

import com.databasepreservation.common.client.models.structure.ViewerMetadata;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SIARDUpdateParameters implements Serializable {
  private ViewerMetadata metadata;
  private ViewerSIARDBundle siardBundle;

  public SIARDUpdateParameters() {}

  public SIARDUpdateParameters(ViewerMetadata metadata, ViewerSIARDBundle siardBundle) {
    this.metadata = metadata;
    this.siardBundle = siardBundle;
  }

  public ViewerMetadata getMetadata() {
    return metadata;
  }

  public ViewerSIARDBundle getSiardBundle() {
    return siardBundle;
  }
}
