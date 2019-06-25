package com.databasepreservation.main.common.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ClientLoggerServiceAsync {

  /**
   * GWT-RPC service asynchronous (client-side) interface
   * 
   * @see ClientLoggerService
   */
  void trace(String classname, String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   *
   * @see ClientLoggerService
   */
  void trace(String classname, String object, Throwable error,
             AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   *
   * @see ClientLoggerService
   */
  void debug(String classname, String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   *
   * @see ClientLoggerService
   */
  void debug(String classname, String object, Throwable error,
             AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   *
   * @see ClientLoggerService
   */
  void info(String classname, String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   *
   * @see ClientLoggerService
   */
  void info(String classname, String object, Throwable error,
            AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   *
   * @see ClientLoggerService
   */
  void warn(String classname, String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   *
   * @see ClientLoggerService
   */
  void warn(String classname, String object, Throwable error,
            AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   *
   * @see ClientLoggerService
   */
  void error(String classname, String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   *
   * @see ClientLoggerService
   */
  void error(String classname, String object, Throwable error,
             AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   *
   * @see ClientLoggerService
   */
  void fatal(String classname, String object, AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   *
   * @see ClientLoggerService
   */
  void fatal(String classname, String object, Throwable error,
             AsyncCallback<Void> callback);

  /**
   * GWT-RPC service asynchronous (client-side) interface
   *
   * @see ClientLoggerService
   */
  void pagehit(String pagename, AsyncCallback<Void> callback);
}
