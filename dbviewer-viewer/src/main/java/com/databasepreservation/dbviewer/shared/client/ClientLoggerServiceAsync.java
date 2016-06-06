package com.databasepreservation.dbviewer.shared.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface ClientLoggerServiceAsync {

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void trace(java.lang.String classname, java.lang.String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void trace(java.lang.String classname, java.lang.String object, java.lang.Throwable error,
    AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void debug(java.lang.String classname, java.lang.String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void debug(java.lang.String classname, java.lang.String object, java.lang.Throwable error,
    AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void info(java.lang.String classname, java.lang.String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void info(java.lang.String classname, java.lang.String object, java.lang.Throwable error, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void warn(java.lang.String classname, java.lang.String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void warn(java.lang.String classname, java.lang.String object, java.lang.Throwable error, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void error(java.lang.String classname, java.lang.String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void error(java.lang.String classname, java.lang.String object, java.lang.Throwable error,
    AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void fatal(java.lang.String classname, java.lang.String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void fatal(java.lang.String classname, java.lang.String object, java.lang.Throwable error,
    AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.dbviewer.shared.client.ClientLoggerService
   */
  void pagehit(java.lang.String pagename, AsyncCallback<Void> callback);

  /**
   * Utility class to get the RPC Async interface from client-side code
   */
  public static final class Util {
    private static ClientLoggerServiceAsync instance;

    public static final ClientLoggerServiceAsync getInstance() {
      if (instance == null) {
        instance = (ClientLoggerServiceAsync) GWT.create(ClientLoggerService.class);
        ServiceDefTarget target = (ServiceDefTarget) instance;
        target.setServiceEntryPoint(GWT.getModuleBaseURL() + "ClientLoggerService");
      }
      return instance;
    }

    private Util() {
      // Utility class should not be instantiated
    }
  }
}
