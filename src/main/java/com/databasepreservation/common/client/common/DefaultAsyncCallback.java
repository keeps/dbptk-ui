/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common;

import com.databasepreservation.common.client.common.utils.AsyncCallbackUtils;
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
