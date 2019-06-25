package com.databasepreservation.main.visualization.client.common.lists;

import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;

import com.databasepreservation.main.common.shared.ViewerStructure.IsIndexed;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class BasicAsyncTableCell<T extends IsIndexed> extends AsyncTableCell<T, Void> {
  public BasicAsyncTableCell() {
    super();
  }

  public BasicAsyncTableCell(Filter filter, Facets facets, String summary, boolean selectable, boolean exportable,
    int initialPageSize, int pageSizeIncrement) {
    super(filter, false, facets, summary, selectable, exportable, initialPageSize, pageSizeIncrement, null);
  }

  public BasicAsyncTableCell(Filter filter, Facets facets, String summary, boolean selectable, boolean exportable) {
    super(filter, false, facets, summary, selectable, exportable, null);
  }
}
