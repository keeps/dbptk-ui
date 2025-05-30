/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.denormalization;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.structure.ViewerTable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DenormalizeConfiguration implements Serializable {
  @Serial
  private static final long serialVersionUID = -2179137349609216271L;
  private String id;
  private String uuid;
  private String version;
  private ViewerJobStatus state;
  private Long job;
  private String tableUUID;
  private String tableID;
  private List<RelatedTablesConfiguration> relatedTables;

  public DenormalizeConfiguration() {
  }

  public DenormalizeConfiguration(String databaseUUID, ViewerTable table) {
    setId(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + table.getUuid());
    setUuid(table.getUuid());
    setTableUUID(table.getUuid());
    setTableID(table.getId());
    setState(ViewerJobStatus.UNKNOWN);
    relatedTables = new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public ViewerJobStatus getState() {
    return state;
  }

  public void setState(ViewerJobStatus state) {
    this.state = state;
  }

  public Long getJob() {
    return job;
  }

  public void setJob(Long job) {
    this.job = job;
  }

  public String getTableUUID() {
    return tableUUID;
  }

  public void setTableUUID(String tableUUID) {
    this.tableUUID = tableUUID;
  }

  public String getTableID() {
    return tableID;
  }

  public void setTableID(String tableID) {
    this.tableID = tableID;
  }

  public List<RelatedTablesConfiguration> getRelatedTables() {
    return relatedTables;
  }

  public RelatedTablesConfiguration getRelatedTable(String uuid) {
    for (RelatedTablesConfiguration relatedTable : relatedTables) {
      if (relatedTable.getUuid().equals(uuid)) {
        return relatedTable;
      } else {
        if (!relatedTable.getRelatedTables().isEmpty()) {
          RelatedTablesConfiguration innerRelatedTable = relatedTable.getRelatedTable(uuid);
          if (innerRelatedTable != null)
            return innerRelatedTable;
        }
      }
    }
    return null;
  }

  public void addRelatedTable(RelatedTablesConfiguration relatedTable) {
    relatedTables.add(relatedTable);
  }

  public void setRelatedTables(List<RelatedTablesConfiguration> relatedTables) {
    this.relatedTables = relatedTables;
  }
}
