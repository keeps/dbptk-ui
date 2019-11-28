package com.databasepreservation.common.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.roda.core.data.exceptions.RODAException;
import org.roda.core.data.v2.user.User;

public interface BrowserServiceAsync {
  /**
   * Utility class to get the RPC Async interface from client-side code
   */
  final class Util {
    private static BrowserServiceAsync instance;

    public static final BrowserServiceAsync getInstance() {
      if (instance == null) {
        instance = GWT.create(BrowserService.class);
      }
      return instance;
    }

    private Util() {
      // Utility class should not be instantiated
    }
  }
}
