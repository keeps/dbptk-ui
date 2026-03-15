package com.databasepreservation.common.server.batch.steps.virtual;

import java.util.ArrayList;

import org.springframework.batch.item.ExecutionContext;

import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.tools.FilterUtils;
import com.databasepreservation.common.server.batch.core.BatchConstants;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualEntityStepUtils {

  public static void enrichVirtualColumnPartitionContext(ExecutionContext partitionContext, TableStatus tableStatus) {
    partitionContext.put(BatchConstants.FILTER_KEY, FilterUtils.filterByTable(new Filter(), tableStatus.getId()));
    partitionContext.put(BatchConstants.FIELDS_KEY, new ArrayList<String>());
  }

  public static void enrichVirtualTablePartitionContext(ExecutionContext partitionContext, TableStatus tableStatus) {
    String sourceTableUUID = tableStatus.getVirtualTableStatus().getSourceTableUUID();
    partitionContext.put(BatchConstants.FILTER_KEY, FilterUtils.filterByTableUUID(new Filter(), sourceTableUUID));
    partitionContext.put(BatchConstants.FIELDS_KEY, new ArrayList<String>());
  }
}
