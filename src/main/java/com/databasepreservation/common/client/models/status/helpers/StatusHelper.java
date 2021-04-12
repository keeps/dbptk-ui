/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.status.helpers;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class StatusHelper {

  private String label;
  private String description;
  private boolean showInTable;
  private boolean showInDetails;
  private boolean showInAdvancedSearch;

  public StatusHelper() {
  }

  public StatusHelper(String label, String description) {
    this.label = label;
    this.description = description;
  }

  public StatusHelper(String label, String description, boolean table, boolean details, boolean advancedSearch) {
    this.label = label;
    this.description = description;
    this.showInTable = table;
    this.showInDetails = details;
    this.showInAdvancedSearch = advancedSearch;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isShowInTable() {
    return showInTable;
  }

  public void setShowInTable(boolean showInTable) {
    this.showInTable = showInTable;
  }

  public boolean isShowInDetails() {
    return showInDetails;
  }

  public void setShowInDetails(boolean showInDetails) {
    this.showInDetails = showInDetails;
  }

  public boolean isShowInAdvancedSearch() {
    return showInAdvancedSearch;
  }

  public void setShowInAdvancedSearch(boolean showInAdvancedSearch) {
    this.showInAdvancedSearch = showInAdvancedSearch;
  }
}