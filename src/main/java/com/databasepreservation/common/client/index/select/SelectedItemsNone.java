/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/roda
 */
package com.databasepreservation.common.client.index.select;

import com.databasepreservation.common.client.index.IsIndexed;

public class SelectedItemsNone<T extends IsIndexed> implements SelectedItems<T> {

  private static final long serialVersionUID = -5364779540199737165L;

  private static SelectedItemsNone selectedItemsNone = new SelectedItemsNone<>();

  public SelectedItemsNone() {
    super();
  }

  @Override
  public String getSelectedClass() {
    return "";
  }

  public static <T extends IsIndexed> SelectedItemsNone<T> create() {
    return selectedItemsNone;
  }

}
