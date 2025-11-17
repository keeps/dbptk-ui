/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.lists.utils;

import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class BasicAsyncTableCell<T extends IsIndexed> extends AsyncTableCell<T, Void> {
  public BasicAsyncTableCell() {
    super();
  }

  public BasicAsyncTableCell(Filter filter, Facets facets, String summary, boolean selectable, boolean exportable,
    boolean copiable, int initialPageSize, int pageSizeIncrement) {
    super(filter, false, facets, summary, selectable, exportable, copiable, initialPageSize, pageSizeIncrement, null);
  }

  public BasicAsyncTableCell(Filter filter, Facets facets, String summary, boolean selectable, boolean exportable,
    boolean copiable) {
    super(filter, false, facets, summary, selectable, exportable, copiable, null);
  }
}
