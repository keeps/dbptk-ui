/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.collection;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
@JsonPropertyOrder({"extractedAndIndexedText"})
public class LobTextExtractionStatus implements Serializable {

  private boolean extractedAndIndexedText;

  public boolean getExtractedAndIndexedText() {
    return extractedAndIndexedText;
  }

  public void setExtractedAndIndexedText(boolean extractedAndIndexedText) {
    this.extractedAndIndexedText = extractedAndIndexedText;
  }
}
