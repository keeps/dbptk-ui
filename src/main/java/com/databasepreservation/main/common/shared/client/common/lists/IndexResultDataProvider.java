/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.main.common.shared.client.common.lists;

import java.io.Serializable;

import org.roda.core.data.v2.index.IndexResult;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IndexResultDataProvider<T extends Serializable> {

  void getData(Sublist sublist, ColumnSortList columnSortList, AsyncCallback<IndexResult<T>> callback);

}
