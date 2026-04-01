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

  public static class LobExtractionTarget implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String physicalPath;
    private final String externalRecordId;

    public LobExtractionTarget(String physicalPath, String externalRecordId) {
      this.physicalPath = physicalPath;
      this.externalRecordId = externalRecordId;
    }

    public String getPhysicalPath() {
      return physicalPath;
    }

    public String getExternalRecordId() {
      return externalRecordId;
    }
  }

  private String uuid;
  private String tableId;
  private final Map<String, List<LobExtractionTarget>> targetsToExtract = new HashMap<>();
  private final Map<String, String> extractedTexts = new HashMap<>();
  private final List<String> columnsToClear = new ArrayList<>();

  public RowLobTextUpdate(String rowUuid) {
    this.uuid = rowUuid;
  }

  public void addTarget(String columnId, LobExtractionTarget target) {
    this.targetsToExtract.computeIfAbsent(columnId, k -> new ArrayList<>()).add(target);
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

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public Map<String, List<LobExtractionTarget>> getTargetsToExtract() {
    return targetsToExtract;
  }

  public Map<String, String> getExtractedTexts() {
    return extractedTexts;
  }

  public List<String> getColumnsToClear() {
    return columnsToClear;
  }

  public boolean hasUpdates() {
    return !extractedTexts.isEmpty() || !columnsToClear.isEmpty() || !targetsToExtract.isEmpty();
  }
}
