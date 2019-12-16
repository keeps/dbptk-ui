/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.client.common.lists.utils;

import java.io.Serializable;

import com.databasepreservation.common.client.index.IndexResult;
import org.fusesource.restygwt.client.MethodCallback;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.google.gwt.user.cellview.client.ColumnSortList;

public interface IndexResultDataProvider<T extends Serializable> {

  void getData(Sublist sublist, ColumnSortList columnSortList, MethodCallback<IndexResult<T>> callback);

}
