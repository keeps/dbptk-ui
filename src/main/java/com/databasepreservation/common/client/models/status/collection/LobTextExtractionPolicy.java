/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
public class LobTextExtractionPolicy implements Serializable {
  private boolean extractAndIndexLobText;

  public LobTextExtractionPolicy() {
    extractAndIndexLobText = false;
  }

  public boolean getExtractAndIndexLobText() {
    return extractAndIndexLobText;
  }

  public void setExtractAndIndexLobText(boolean extractAndIndexLobText) {
    this.extractAndIndexLobText = extractAndIndexLobText;
  }
}
