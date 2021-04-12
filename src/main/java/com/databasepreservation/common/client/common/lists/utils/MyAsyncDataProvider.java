/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.lists.utils;

import java.io.Serializable;
import java.util.Date;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.index.IndexResult;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

//import org.roda.wui.common.client.widgets.Toast;

public abstract class MyAsyncDataProvider<T extends Serializable> extends AsyncDataProvider<T> {

  private final CellTable<T> display;
  private final IndexResultDataProvider<T> dataProvider;
  private int rowCount;
  private Date date;

  public MyAsyncDataProvider(CellTable<T> display, IndexResultDataProvider<T> dataProvider) {
    super();
    this.display = display;
    this.dataProvider = dataProvider;
  }

  @Override
  protected void onRangeChanged(HasData<T> display) {
    fetch(display, new DefaultMethodCallback<Void>() {
      @Override
      public void onSuccess(Method method, Void aVoid) {
        // do nothing
      }
    });
  }

  private void fetch(final HasData<T> display, final MethodCallback<Void> callback) {
    // Get the new range.
    final Range range = display.getVisibleRange();

    // Get sorting
    ColumnSortList columnSortList = this.display.getColumnSortList();

    // Query the data asynchronously.
    final int start = range.getStart();
    int length = range.getLength();
    dataProvider.getData(new Sublist(start, length), columnSortList, new MethodCallback<IndexResult<T>>() {
      @Override
      public void onFailure(Method method, Throwable throwable) {
        callback.onFailure(method, throwable);
      }

      @Override
      public void onSuccess(Method method, IndexResult<T> result) {
        if (result != null) {
          rowCount = (int) result.getTotalCount();
          date = result.getDate();
          updateRowData((int) result.getOffset(), result.getResults());
          updateRowCount(rowCount, true);
          // ValueChangeEvent.fire(AsyncTableCell.this, result);
          fireChangeEvent(result);
        } else {
          // search not yet ready, deliver empty result
        }
        callback.onSuccess(method, null);
      }
    });
  }

  protected abstract void fireChangeEvent(IndexResult<T> result);

  public void update(final MethodCallback<Void> callback) {
    fetch(display, callback);
  }

  public void update() {
    update(new DefaultMethodCallback<Void>() {
      @Override
      public void onSuccess(Method method, Void aVoid) {
        // do nothing
      }
    });
  }

  public int getRowCount() {
    return rowCount;
  }

  public Date getDate() {
    return date;
  }

}
