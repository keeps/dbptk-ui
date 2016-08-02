package com.databasepreservation.visualization.client.common;

import com.databasepreservation.visualization.client.common.utils.AsyncCallbackUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Asynchronous callback with a default failure handler
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class DefaultAsyncCallback<T> implements AsyncCallback<T> {
  @Override
  public void onFailure(Throwable caught) {
    AsyncCallbackUtils.defaultFailureTreatment(caught);
  }
}
