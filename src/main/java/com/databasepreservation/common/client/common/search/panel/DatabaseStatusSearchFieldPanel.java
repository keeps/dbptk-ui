/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.search.panel;

import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.tools.Humanize;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DatabaseStatusSearchFieldPanel extends BaseEnumSearchFieldPanel<ViewerDatabaseStatus> {
  public DatabaseStatusSearchFieldPanel(SearchField searchField) {
    super(searchField, ViewerDatabaseStatus.class);
  }

  @Override
  protected String getDisplayLabel(ViewerDatabaseStatus status) {
    return Humanize.databaseStatus(status);
  }
}
