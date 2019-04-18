package com.databasepreservation.visualization.client.common.lists;

import com.databasepreservation.visualization.shared.ViewerStructure.IsIndexed;
import org.roda.core.data.v2.index.facet.Facets;
import org.roda.core.data.v2.index.filter.Filter;

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
