package com.databasepreservation.visualization.shared.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ClientLoggerServiceAsync {

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void trace(java.lang.String classname, java.lang.String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void trace(java.lang.String classname, java.lang.String object, java.lang.Throwable error,
    AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void debug(java.lang.String classname, java.lang.String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void debug(java.lang.String classname, java.lang.String object, java.lang.Throwable error,
    AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void info(java.lang.String classname, java.lang.String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void info(java.lang.String classname, java.lang.String object, java.lang.Throwable error, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void warn(java.lang.String classname, java.lang.String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void warn(java.lang.String classname, java.lang.String object, java.lang.Throwable error, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void error(java.lang.String classname, java.lang.String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void error(java.lang.String classname, java.lang.String object, java.lang.Throwable error,
    AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void fatal(java.lang.String classname, java.lang.String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void fatal(java.lang.String classname, java.lang.String object, java.lang.Throwable error,
    AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see com.databasepreservation.visualization.shared.client.ClientLoggerService
   */
  void pagehit(java.lang.String pagename, AsyncCallback<Void> callback);
}
