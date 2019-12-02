package com.databasepreservation.common.api.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.services.ClientLoggerService;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@Service
@Path(ViewerConstants.ENDPOINT_CLIENT_LOGGER)
public class ClientLoggerResource implements ClientLoggerService {
  @Context
  private HttpServletRequest request;

  private String getUserInfo() {
    String ret;
    String address = request.getRemoteAddr();
    ret = "[" + address + "] ";
    return ret;
  }

  @Override
  public void trace(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.trace(getUserInfo() + object);
  }

  @Override
  public void trace(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.trace(getUserInfo() + object, error);
  }

  @Override
  public void debug(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.debug(getUserInfo() + object);
  }

  @Override
  public void debug(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.debug(getUserInfo() + object, error);
  }

  @Override
  public void info(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.info(getUserInfo() + object);
  }

  @Override
  public void info(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.info(getUserInfo() + object, error);
  }

  @Override
  public void warn(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.warn(getUserInfo() + object);
  }

  @Override
  public void warn(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.warn(getUserInfo() + object, error);
  }

  @Override
  public void error(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.error(getUserInfo() + object);
    sendError(classname, object, null);
  }

  @Override
  public void error(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.error(getUserInfo() + object, error);
    sendError(classname, object, error);
  }

  @Override
  public void fatal(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.error(getUserInfo() + object);
    sendError(classname, object, null);
  }

  @Override
  public void fatal(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.error(getUserInfo() + object, error);
    sendError(classname, object, error);
  }

  @Override
  public void pagehit(String pagename) {
    Logger logger = LoggerFactory.getLogger(ClientLoggerResource.class);
    // try {
    // RODAClient rodaClient = RodaClientFactory.getRodaWuiClient();
    // String username = RodaClientFactory.getRodaClient(
    // this.getThreadLocalRequest().getSession()).getUsername();
    // LogEntryParameter[] parameters = new LogEntryParameter[] {
    // new LogEntryParameter("hostname", getThreadLocalRequest()
    // .getRemoteHost()),
    // new LogEntryParameter("address", getThreadLocalRequest()
    // .getRemoteAddr()),
    // new LogEntryParameter("user", getThreadLocalRequest()
    // .getRemoteUser()),
    // new LogEntryParameter("pagename", pagename) };
    //
    // LogEntry logEntry = new LogEntry();
    // logEntry.setAction(LOG_ACTION_WUI_PAGEHIT);
    // logEntry.setParameters(parameters);
    // logEntry.setUsername(username);
    //
    // rodaClient.getLoggerService().addLogEntry(logEntry);
    // } catch (RemoteException e) {
    // logger.error("Error logging page hit", e);
    // } catch (RODAClientException e) {
    // logger.error("Error logging page hit", e);
    // } catch (LoginException e) {
    // logger.error("Error logging page hit", e);
    // } catch (LoggerException e) {
    // logger.error("Error logging page hit", e);
    // }
  }

  public void destroy() {
    LogManager.shutdown();
  }

  public void sendError(String classname, String message, Throwable error) {
    Logger logger = LoggerFactory.getLogger(ClientLoggerResource.class);

    // try {
    // RODAClient rodaClient = RodaClientFactory.getRodaWuiClient();
    // String username = RodaClientFactory.getRodaClient(
    // this.getThreadLocalRequest().getSession()).getUsername();
    // List<LogEntryParameter> parameters = new Vector<>();
    // parameters.add(new LogEntryParameter("hostname",
    // getThreadLocalRequest().getRemoteHost()));
    // parameters.add(new LogEntryParameter("address",
    // getThreadLocalRequest().getRemoteAddr()));
    // parameters.add(new LogEntryParameter("port",
    // getThreadLocalRequest().getRemotePort() + ""));
    // parameters.add(new LogEntryParameter("classname", classname));
    // parameters.add(new LogEntryParameter("error", message));
    // if (error != null) {
    // parameters.add(new LogEntryParameter("message", error.getMessage()));
    // }
    // //
    // LogEntry logEntry = new LogEntry();
    // logEntry.setAction(LOG_ACTION_WUI_ERROR);
    // logEntry.setParameters(parameters.toArray(new
    // LogEntryParameter[parameters.size()]));
    // logEntry.setUsername(username);
    //
    // rodaClient.getLoggerService().addLogEntry(logEntry);
    // } catch (RemoteException e) {
    // logger.error("Error logging login", e);
    // } catch (LoginException e) {
    // logger.error("Error logging login", e);
    // } catch (LoggerException e) {
    // logger.error("Error logging login", e);
    // } catch (RODAClientException e) {
    // logger.error("Error logging login", e);
    // }
  }
}
