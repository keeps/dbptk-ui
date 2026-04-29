/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.search.panel;

import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseConfigurationStatus;
import com.databasepreservation.common.client.tools.Humanize;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DatabaseConfigStatusSearchFieldPanel extends BaseEnumSearchFieldPanel<ViewerDatabaseConfigurationStatus> {
  public DatabaseConfigStatusSearchFieldPanel(SearchField searchField) {
    super(searchField, ViewerDatabaseConfigurationStatus.class);
  }

  @Override
  protected String getDisplayLabel(ViewerDatabaseConfigurationStatus status) {
    return Humanize.databaseConfigurationStatus(status);
  }
}
