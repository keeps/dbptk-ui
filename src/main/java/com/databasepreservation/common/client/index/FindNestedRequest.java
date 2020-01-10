package com.databasepreservation.common.client.index;

import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.sort.Sorter;
import org.roda.core.data.v2.index.sublist.Sublist;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class FindNestedRequest extends FindRequest {

  public List<String> nestedTables;

  public FindNestedRequest() {
    this(null, new Filter(), new Sorter(), new Sublist(), new Facets(), false, new ArrayList(), new ArrayList());
  }

  public FindNestedRequest(String classToReturn, Filter filter, Sorter sorter, Sublist sublist, Facets facets, boolean exportFacets, List<String> fieldsToReturn, List<String> nestedTables) {
    super(classToReturn, filter, sorter, sublist, facets, exportFacets, fieldsToReturn);
    this.nestedTables = nestedTables;
  }
}
