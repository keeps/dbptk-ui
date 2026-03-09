/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.tools;

import java.util.HashSet;
import java.util.Set;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class FilterUtils {
  public static Filter filterByTable(Filter filter, String tableId) {
    return filterByTableIdOrUUID(filter, ViewerConstants.SOLR_ROWS_TABLE_ID, tableId);
  }

  public static Filter filterByTableUUID(Filter filter, String tableUUID) {
    return filterByTableIdOrUUID(filter, ViewerConstants.SOLR_ROWS_TABLE_UUID, tableUUID);
  }

  private static Filter filterByTableIdOrUUID(Filter filter, String parameterName, String value) {
    Set<FilterParameter> toRemove = new HashSet<>();
    boolean alreadyPresent = false;

    // look for other table filters, and note if one of them is the one we want
    for (FilterParameter param : filter.getParameters()) {
      if (param instanceof SimpleFilterParameter) {
        SimpleFilterParameter simpleParam = (SimpleFilterParameter) param;

        if (parameterName.equals(simpleParam.getName())) {
          if (value.equals(simpleParam.getValue())) {
            alreadyPresent = true;
          } else {
            toRemove.add(param);
          }
        }
      }
    }

    // remove other schemas filters. intersection between schemas is always empty
    if (!toRemove.isEmpty()) {
      filter.getParameters().removeAll(toRemove);
    }

    // add the new filter if it wasn't already present
    if (!alreadyPresent) {
      filter.add(new SimpleFilterParameter(parameterName, value));
    }

    return filter;
  }

  public static Filter getTableFilter(String tableId) {
    return new Filter(new SimpleFilterParameter(ViewerConstants.SOLR_ROWS_TABLE_ID, tableId));
  }
}
