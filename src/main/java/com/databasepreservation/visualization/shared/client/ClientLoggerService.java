/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.shared.client;

import org.roda.core.data.exceptions.LoggerException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author Luis Faria
 *
 */
public interface ClientLoggerService extends RemoteService {

  /**
   * logger service URI
   */
  String SERVICE_URI = "wuilogger";

  /**
   * Utilities
   *
   */
  class Util {

    /**
     * Get service instance
     *
     * @return
     */
    public static ClientLoggerServiceAsync getInstance() {

      ClientLoggerServiceAsync instance = GWT.create(ClientLoggerService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }

  /**
   * Log at trace level
   *
   * @param classname
   * @param object
   */
  void trace(String classname, String object);

  /**
   * Log at trace level
   *
   * @param classname
   * @param object
   * @param error
   */
  void trace(String classname, String object, Throwable error);

  /**
   * Log at debug level
   *
   * @param classname
   * @param object
   */
  void debug(String classname, String object);

  /**
   * Log at debug level
   *
   * @param classname
   * @param object
   * @param error
   */
  void debug(String classname, String object, Throwable error);

  /**
   * Log at info level
   *
   * @param classname
   * @param object
   */
  void info(String classname, String object);

  /**
   * Log at info level
   *
   * @param classname
   * @param object
   * @param error
   */
  void info(String classname, String object, Throwable error);

  /**
   * Log at warn level
   *
   * @param classname
   * @param object
   */
  void warn(String classname, String object);

  /**
   * Log at warn level
   *
   * @param classname
   * @param object
   * @param error
   */
  void warn(String classname, String object, Throwable error);

  /**
   * Log at error level
   *
   * @param classname
   * @param object
   */
  void error(String classname, String object);

  /**
   * Log at error level
   *
   * @param classname
   * @param object
   * @param error
   */
  void error(String classname, String object, Throwable error);

  /**
   * Log at fatal level
   *
   * @param classname
   * @param object
   */
  void fatal(String classname, String object);

  /**
   * Log at fatal level
   *
   * @param classname
   * @param object
   * @param error
   */
  void fatal(String classname, String object, Throwable error);

  /**
   * Log a page hit
   *
   * @param pagename
   * @throws LoginException
   * @throws LoggerException
   */
  void pagehit(String pagename) throws LoggerException;

}
