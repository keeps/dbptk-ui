/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.wizard.filter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MerkleTreeFilterParameters implements Serializable {

  private Integer dbptkFilterIndex;
  private Map<String, String> values = new HashMap<>();

  public MerkleTreeFilterParameters() {
  }

  public MerkleTreeFilterParameters(Map<String, String> values) {
    this.values = values;
  }

  public Integer getDbptkFilterIndex() {
    return dbptkFilterIndex;
  }

  public void setDbptkFilterIndex(Integer dbptkFilterIndex) {
    this.dbptkFilterIndex = dbptkFilterIndex;
  }

  public Map<String, String> getValues() {
    return values;
  }

  public void setValues(Map<String, String> values) {
    this.values = values;
  }
}
