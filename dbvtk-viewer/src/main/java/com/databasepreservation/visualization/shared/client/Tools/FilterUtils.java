package com.databasepreservation.visualization.shared.client.Tools;

import java.util.HashSet;
import java.util.Set;

import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;

import com.databasepreservation.visualization.shared.ViewerSafeConstants;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class FilterUtils {
  public static Filter filterByTable(Filter filter, String tableId) {
    Set<FilterParameter> otherTableFilters = new HashSet<>();
    boolean alreadyPresent = false;

    // look for other table filters, and note if one of them is the one we want
    for (FilterParameter filterParameter : filter.getParameters()) {
      if (filterParameter instanceof SimpleFilterParameter) {
        SimpleFilterParameter simpleFilterParameter = (SimpleFilterParameter) filterParameter;
        if (ViewerSafeConstants.SOLR_TABLE_ID.equals(simpleFilterParameter.getName())) {
          if (tableId.equals(simpleFilterParameter.getValue())) {
            alreadyPresent = true;
          } else {
            otherTableFilters.add(filterParameter);
          }
        }
      }
    }

    // remove other tables filters. intersection between tables is always empty
    for (FilterParameter otherTableFilter : otherTableFilters) {
      filter.getParameters().remove(otherTableFilter);
    }

    if (!alreadyPresent) {
      filter.add(new SimpleFilterParameter(ViewerSafeConstants.SOLR_TABLE_ID, tableId));
    }

    return filter;
  }
}
