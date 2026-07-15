/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.batch.steps.partition;

import org.springframework.batch.item.ExecutionContext;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@FunctionalInterface
public interface PartitionContextEnricher<T> {
  void enrich(ExecutionContext partitionContext, T partitionItem);
}
