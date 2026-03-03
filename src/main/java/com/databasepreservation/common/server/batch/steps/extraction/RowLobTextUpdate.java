package com.databasepreservation.common.server.batch.steps.extraction;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.index.IsIndexed;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class RowLobTextUpdate extends IsIndexed implements Serializable {
  @Serial
  private static final long serialVersionUID = -8620226350367163522L;

  private String uuid;
  private final Map<String, String> extractedTexts = new HashMap<>();
  private final List<String> columnsToClear = new ArrayList<>();

  public RowLobTextUpdate(String rowUuid) {
    this.uuid = rowUuid;
  }

  public void addText(String columnId, String text) {
    this.extractedTexts.put(columnId, text);
  }

  public void markForClear(String columnId) {
    this.columnsToClear.add(columnId);
  }

  @Override
  public String getUuid() {
    return uuid;
  }

  @Override
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public Map<String, String> getExtractedTexts() {
    return extractedTexts;
  }

  public List<String> getColumnsToClear() {
    return columnsToClear;
  }

  public boolean hasUpdates() {
    return !extractedTexts.isEmpty() || !columnsToClear.isEmpty();
  }
}
