/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.dbviewer.shared.client;

import org.roda.core.data.common.LoginException;
import org.roda.core.data.exceptions.LoggerException;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Luis Faria
 *
 */
public interface ClientLoggerServiceAsync {

  /**
   * Log at trace level
   *
   * @param classname
   * @param object
   */
  public void trace(String classname, String object, AsyncCallback<Void> callback);

  /**
   * Log at trace level
   *
   * @param classname
   * @param object
   * @param error
   */
  public void trace(String classname, String object, Throwable error, AsyncCallback<Void> callback);

  /**
   * Log at debug level
   *
   * @param classname
   * @param object
   */
  public void debug(String classname, String object, AsyncCallback<Void> callback);

  /**
   * Log at debug level
   *
   * @param classname
   * @param object
   * @param error
   */
  public void debug(String classname, String object, Throwable error, AsyncCallback<Void> callback);

  /**
   * Log at info level
   *
   * @param classname
   * @param object
   */
  public void info(String classname, String object, AsyncCallback<Void> callback);

  /**
   * Log at info level
   *
   * @param classname
   * @param object
   * @param error
   */
  public void info(String classname, String object, Throwable error, AsyncCallback<Void> callback);

  /**
   * Log at warn level
   *
   * @param classname
   * @param object
   */
  public void warn(String classname, String object, AsyncCallback<Void> callback);

  /**
   * Log at warn level
   *
   * @param classname
   * @param object
   * @param error
   */
  public void warn(String classname, String object, Throwable error, AsyncCallback<Void> callback);

  /**
   * Log at error level
   *
   * @param classname
   * @param object
   */
  public void error(String classname, String object, AsyncCallback<Void> callback);

  /**
   * Log at error level
   *
   * @param classname
   * @param object
   * @param error
   */
  public void error(String classname, String object, Throwable error, AsyncCallback<Void> callback);

  /**
   * Log at fatal level
   *
   * @param classname
   * @param object
   */
  public void fatal(String classname, String object, AsyncCallback<Void> callback);

  /**
   * Log at fatal level
   *
   * @param classname
   * @param object
   * @param error
   */
  public void fatal(String classname, String object, Throwable error, AsyncCallback<Void> callback);

  /**
   * Log a page hit
   *
   * @param pagename
   * @throws LoginException
   * @throws LoggerException
   */
  public void pagehit(String pagename, AsyncCallback<Void> callback) throws LoginException, LoggerException;

}
