/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.databasepreservation.common.client.services.ClientLoggerService;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.shared.SerializableThrowable;
import com.google.gwt.logging.client.DevelopmentModeLogHandler;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Luis Faria
 *
 */
public class ClientLogger implements IsSerializable {
  // log levels
  /**
   * Trace level
   */
  public static final int TRACE = 0;

  /**
   * Debug level
   */
  public static final int DEBUG = 1;

  /**
   * Information level
   */
  public static final int INFO = 2;

  /**
   * Warning level
   */
  public static final int WARN = 3;

  /**
   * Error level
   */
  public static final int ERROR = 4;

  /**
   * Fatal error level
   */
  public static final int FATAL = 5;

  private static final int CURRENT_LOG_LEVEL = TRACE;

  private static boolean SHOW_ERROR_MESSAGES = false;

  private String classname;
  private Logger logger;

  /**
   * Create a new client logger
   */
  public ClientLogger() {
    logger = Logger.getLogger("");
    logger.addHandler(new DevelopmentModeLogHandler());
  }

  /**
   * Create a new client logger
   *
   * @param classname
   */
  public ClientLogger(String classname) {
    this.classname = classname;
    logger = Logger.getLogger(classname);
    logger.addHandler(new DevelopmentModeLogHandler());
  }

  /**
   * Set the uncaught exception handler
   */
  public static void setUncaughtExceptionHandler() {
    GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      ClientLogger clientlogger = new ClientLogger("Uncaught");

      public void onUncaughtException(Throwable e) {
        clientlogger.fatal("Uncaught Exception: " + e.getMessage(), e);
      }

    });
  }

  /**
   * Log a trace message
   *
   * @param message
   */
  public void trace(final String message) {
    if (CURRENT_LOG_LEVEL <= TRACE) {
      MethodCallback<Void> errorcallback = new MethodCallback<Void>() {
        @Override
        public void onFailure(Method method, Throwable caught) {
          GWT.log(message, null);
          GWT.log("Error while logging another error", caught);
        }

        @Override
        public void onSuccess(Method method, Void result) {
          // do nothing
        }
      };
      ClientLoggerService.Util.call(errorcallback).log("trace", classname, message);
    }
  }

  /**
   * Log a trace message and error
   *
   * @param message
   * @param error
   */
  public void trace(final String message, final Throwable error) {
    if (CURRENT_LOG_LEVEL <= TRACE) {

      MethodCallback<Void> errorcallback = new MethodCallback<Void>() {
        @Override
        public void onFailure(Method method, Throwable caught) {
          GWT.log(message, error);
          GWT.log("Error while logging another error", caught);
        }

        @Override
        public void onSuccess(Method method, Void result) {
          GWT.log(message, error);
        }
      };
      ClientLoggerService.Util.call(errorcallback).detailedLog("trace", classname, message,
        SerializableThrowable.fromThrowable(error));
    }
  }

  /**
   * Log a debug message
   *
   * @param message
   */
  public void debug(final String message) {
    GWT.log(message);
    if (CURRENT_LOG_LEVEL <= DEBUG) {
      MethodCallback<Void> errorcallback = new MethodCallback<Void>() {
        @Override
        public void onFailure(Method method, Throwable caught) {
          GWT.log(message, null);
          GWT.log("Error while logging another error", caught);
        }

        @Override
        public void onSuccess(Method method, Void result) {
          // do nothing
        }
      };
      ClientLoggerService.Util.call(errorcallback).log("debug", classname, message);
    }
  }

  /**
   * Log a debug message and error
   *
   * @param object
   * @param error
   */
  public void debug(final String object, final Throwable error) {
    if (CURRENT_LOG_LEVEL <= DEBUG) {
      MethodCallback<Void> errorcallback = new MethodCallback<Void>() {
        @Override
        public void onFailure(Method method, Throwable caught) {
          GWT.log(object, error);
          GWT.log("Error while logging another error", caught);
        }

        @Override
        public void onSuccess(Method method, Void result) {
          // do nothing
          GWT.log(object, error);
        }
      };
      ClientLoggerService.Util.call(errorcallback).detailedLog("debug", classname, object,
        SerializableThrowable.fromThrowable(error));
    }
  }

  /**
   * Log a information message
   *
   * @param message
   */
  public void info(final String message) {
    if (CURRENT_LOG_LEVEL <= INFO) {
      MethodCallback<Void> errorcallback = new MethodCallback<Void>() {
        @Override
        public void onFailure(Method method, Throwable caught) {
          GWT.log(message, null);
          GWT.log("Error while logging another error", caught);
        }

        @Override
        public void onSuccess(Method method, Void result) {
          // do nothing
        }
      };
      ClientLoggerService.Util.call(errorcallback).log("info", classname, message);
    }
  }

  /**
   * Log an information message and error
   *
   * @param message
   * @param error
   */
  public void info(final String message, final Throwable error) {
    if (CURRENT_LOG_LEVEL <= INFO) {
      MethodCallback<Void> errorcallback = new MethodCallback<Void>() {
        @Override
        public void onFailure(Method method, Throwable caught) {
          GWT.log(message, error);
          GWT.log("Error while logging another error", caught);
        }

        @Override
        public void onSuccess(Method method, Void result) {
          // do nothing
          GWT.log(message, error);
        }
      };
      ClientLoggerService.Util.call(errorcallback).detailedLog("info", classname, message,
        SerializableThrowable.fromThrowable(error));
    }
  }

  /**
   * Log a warning message
   *
   * @param message
   */
  public void warn(final String message) {
    if (CURRENT_LOG_LEVEL <= WARN) {
      MethodCallback<Void> errorcallback = new MethodCallback<Void>() {
        @Override
        public void onFailure(Method method, Throwable caught) {
          GWT.log(message, null);
          GWT.log("Error while logging another error", caught);
        }

        @Override
        public void onSuccess(Method method, Void result) {
          // do nothing
        }
      };
      ClientLoggerService.Util.call(errorcallback).log("warn", classname, message);
    }
  }

  /**
   * Log a warning message and error
   *
   * @param message
   * @param error
   */
  public void warn(final String message, final Throwable error) {
    if (CURRENT_LOG_LEVEL <= WARN) {
      MethodCallback<Void> errorcallback = new MethodCallback<Void>() {
        @Override
        public void onFailure(Method method, Throwable caught) {
          GWT.log(message, error);
          GWT.log("Error while logging another error", caught);
        }

        @Override
        public void onSuccess(Method method, Void result) {
          GWT.log(message, error);
        }
      };
      ClientLoggerService.Util.call(errorcallback).detailedLog("warn", classname, message,
        SerializableThrowable.fromThrowable(error));
    }
  }

  /**
   * Log an error message
   *
   * @param message
   */
  public void error(final String message) {
    if (CURRENT_LOG_LEVEL <= ERROR) {
      MethodCallback<Void> errorCallback = new MethodCallback<Void>() {
        @Override
        public void onFailure(Method method, Throwable caught) {
          GWT.log(message, null);
          GWT.log("Error while logging another error", caught);
        }

        @Override
        public void onSuccess(Method method, Void result) {
          // do nothing
        }
      };
      GWT.log(message, null);
      ClientLoggerService.Util.call(errorCallback).log("error", classname, message);

      // if (SHOW_ERROR_MESSAGES) {
      // Toast.showError(message);
      // }
    }
  }

  /**
   * Log an error message and error
   *
   * @param message
   * @param error
   */
  public void error(final String message, final Throwable error) {
    // FIXME should this be done if internal authentication is being used? I
    // don't think so
    if (CURRENT_LOG_LEVEL <= ERROR) {
      MethodCallback<Void> errorcallback = new MethodCallback<Void>() {
        @Override
        public void onFailure(Method method, Throwable caught) {
          logger.log(Level.SEVERE, message, error);
          logger.log(Level.SEVERE, "Error while logging another error", caught);
        }

        @Override
        public void onSuccess(Method method, Void result) {
          logger.log(Level.SEVERE, message, error);
        }
      };

      ClientLoggerService.Util.call(errorcallback).detailedLog("error", classname, message,
        SerializableThrowable.fromThrowable(error));
      // if (SHOW_ERROR_MESSAGES) {
      // Toast.showError(message, error.getMessage()
      // + (error.getCause() != null ? "\nCause: " +
      // error.getCause().getMessage() : ""));
      // }
    }
  }

  /**
   * Log a fatal message
   *
   * @param message
   */
  public void fatal(final String message) {
    if (CURRENT_LOG_LEVEL <= FATAL) {
      MethodCallback<Void> errorcallback = new MethodCallback<Void>() {
        @Override
        public void onFailure(Method method, Throwable caught) {
          GWT.log(message, null);
          GWT.log("Error while logging another error", caught);
        }

        @Override
        public void onSuccess(Method method, Void result) {
          // do nothing
        }
      };
      GWT.log(message);
      logger.log(Level.SEVERE, message);
      ClientLoggerService.Util.call(errorcallback).log("fatal", classname, message);
      // if (SHOW_ERROR_MESSAGES) {
      // Toast.showError(message);
      // }
    }
  }

  /**
   * Log a fatal message and error
   *
   * @param message
   * @param error
   */
  public void fatal(final String message, final Throwable error) {
    if (CURRENT_LOG_LEVEL <= FATAL) {
      MethodCallback<Void> errorcallback = new MethodCallback<Void>() {
        @Override
        public void onFailure(Method method, Throwable caught) {
          GWT.log(message, error);
          GWT.log("Error while logging another error", caught);
          logger.log(Level.SEVERE, message, error);
        }

        @Override
        public void onSuccess(Method method, Void result) {
          GWT.log(message, error);
          logger.log(Level.SEVERE, message, error);
        }
      };

      ClientLoggerService.Util.call(errorcallback).detailedLog("fatal", classname, message,
        SerializableThrowable.fromThrowable(error));

      // if (SHOW_ERROR_MESSAGES) {
      // Toast.showError(message, error.getMessage()
      // + (error.getCause() != null ? "\nCause: " +
      // error.getCause().getMessage() : ""));
      // }
    }
  }

  /**
   * Get logging class name
   *
   * @return the name of the class being logged
   */
  public String getClassname() {
    return classname;
  }

  /**
   * Set class name
   *
   * @param classname
   *          the name of class being logged
   */
  public void setClassname(String classname) {
    this.classname = classname;
  }

}
