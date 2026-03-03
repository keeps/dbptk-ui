/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.collection;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import com.databasepreservation.common.client.models.status.IsProcessable;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Alexandre Flores <aflores@keep.pt>
 */
@JsonPropertyOrder({"extractedAndIndexedText"})
public class LobTextExtractionStatus implements Serializable, IsProcessable {

  @Serial
  private static final long serialVersionUID = 2293380102857714674L;

  private boolean extractedAndIndexedText;
  private ProcessingState processingState;
  private Date lastUpdatedDate;
  private Date lastExecutionDate;

  public boolean getExtractedAndIndexedText() {
    return extractedAndIndexedText;
  }

  public void setExtractedAndIndexedText(boolean extractedAndIndexedText) {
    this.extractedAndIndexedText = extractedAndIndexedText;
  }

  @Override
  public ProcessingState getProcessingState() {
    return processingState;
  }

  public void setProcessingState(ProcessingState processingState) {
    this.processingState = processingState;
  }

  @Override
  public Date getLastUpdatedDate() {
    return lastUpdatedDate;
  }

  public void setLastUpdatedDate(Date lastUpdatedDate) {
    this.lastUpdatedDate = lastUpdatedDate;
  }

  @Override
  public Date getLastExecutionDate() {
    return lastExecutionDate;
  }

  public void setLastExecutionDate(Date lastExecutionDate) {
    this.lastExecutionDate = lastExecutionDate;
  }
}
