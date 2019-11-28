package com.databasepreservation.common.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * The client side stub for the browser service.
 */
@RemoteServiceRelativePath("browse")
public interface BrowserService extends RemoteService {
  /**
   * Service location
   */
  String SERVICE_URI = "browse";

  /**
   * Utilities
   */
  class Util {

    /**
     * @return the singleton instance
     */
    public static BrowserServiceAsync getInstance() {
      BrowserServiceAsync instance = GWT.create(BrowserService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }
}
