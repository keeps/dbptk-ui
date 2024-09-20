package com.databasepreservation.common.client.common.lists.utils;

import java.util.function.Supplier;

import com.databasepreservation.common.client.index.IsIndexed;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ListBuilder<T extends IsIndexed> {
  private final AsyncTableCellOptions<T> options;
  private final Supplier<AsyncTableCell<T, Void>> listSupplier;

  public ListBuilder(Supplier<AsyncTableCell<T, Void>> listSupplier, AsyncTableCellOptions<T> options) {
    this.options = options;
    this.listSupplier = listSupplier;
  }

  public AsyncTableCell<T, Void> build() {
    return listSupplier.get();
  }

  public AsyncTableCellOptions<T> getOptions() {
    return options;
  }
}
