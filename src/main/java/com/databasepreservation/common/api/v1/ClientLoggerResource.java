/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.services.ClientLoggerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gwt.core.server.StackTraceDeobfuscator;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
@RestController
@RequestMapping(path = ViewerConstants.ENDPOINT_CLIENT_LOGGER)
public class ClientLoggerResource implements ClientLoggerService {
  @Autowired
  private HttpServletRequest request;

  private StackTraceDeobfuscator deobfuscator = new StackTraceDeobfuscator() {
    @Override
    protected InputStream openInputStream(String fileName) {
      // Try Server module first
      InputStream is = getClass()
          .getResourceAsStream("/deploy/com.databasepreservation.server.Server/symbolMaps/" + fileName);
      if (is == null) {
        // Fallback to Desktop module
        is = getClass().getResourceAsStream("/deploy/com.databasepreservation.desktop.Desktop/symbolMaps/" + fileName);
      }
      return is;
    }
  };

  private String processLogMessage(String object) {
    if (object == null || !object.contains("\"stackTrace\"")) {
      return object; // Not a stack trace JSON, return as-is
    }

    // The client must tell us which strong name to look for
    String permutationName = request.getHeader("X-GWT-Permutation");
    if (permutationName == null || permutationName.isEmpty()) {
      return object + " \n[Deobfuscation skipped: X-GWT-Permutation header missing]";
    }

    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(object);
      JsonNode stackTraceNode = root.get("stackTrace");

      if (stackTraceNode != null && stackTraceNode.isArray()) {
        List<StackTraceElement> elements = new ArrayList<>();
        for (JsonNode node : stackTraceNode) {
          String className = node.path("className").asText("Unknown");
          String methodName = node.path("methodName").asText("Unknown");
          String fileName = node.path("fileName").asText("Unknown");
          int lineNumber = node.path("lineNumber").asInt(-1);

          elements.add(new StackTraceElement(className, methodName, fileName, lineNumber));
        }

        // Attach to a dummy throwable for the deobfuscator
        Throwable dummy = new Throwable("GWT Client Crash");
        dummy.setStackTrace(elements.toArray(new StackTraceElement[0]));

        // Magic happens here!
        deobfuscator.deobfuscateStackTrace(dummy, permutationName);

        // Build a nice, readable multi-line string for your log files
        StringBuilder sb = new StringBuilder();
        sb.append("Deobfuscated Client Error:\n");
        for (StackTraceElement ste : dummy.getStackTrace()) {
          sb.append("\tat ").append(ste.toString()).append("\n");
        }
        return sb.toString();
      }
    } catch (Exception e) {
      // If Jackson fails to parse it, just safely return the original string
      LoggerFactory.getLogger(ClientLoggerResource.class).debug("Failed to parse log JSON", e);
    }

    return object;
  }

  private String getUserInfo() {
    String ret;
    String address = request.getRemoteAddr();
    ret = "[" + address + "] ";
    return ret;
  }

  @Override
  public Void log(String type, String classname, String object) {
    switch (type) {
      case "trace":
        trace(classname, object);
        break;
      case "debug":
        debug(classname, object);
        break;
      case "warn":
        warn(classname, object);
        break;
      case "error":
        error(classname, object);
        break;
      case "fatal":
        fatal(classname, object);
        break;
      case "info":
      default:
        info(classname, object);
        break;
    }
    return null;
  }

  @Override
  public Void detailedLog(String type, String classname, String object, Throwable error) {
    switch (type) {
      case "trace":
        trace(classname, object, error);
        break;
      case "debug":
        debug(classname, object, error);
        break;
      case "warn":
        warn(classname, object, error);
        break;
      case "error":
        error(classname, object, error);
        break;
      case "fatal":
        fatal(classname, object, error);
        break;
      case "info":
      default:
        info(classname, object, error);
        break;
    }
    return null;
  }

  private void trace(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.trace(getUserInfo() + object);
  }

  private void trace(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.trace(getUserInfo() + object, error);
  }

  private void debug(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.debug(getUserInfo() + object);
  }

  private void debug(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.debug(getUserInfo() + object, error);
  }

  private void info(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.info(getUserInfo() + object);
  }

  private void info(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.info(getUserInfo() + object, error);
  }

  private void warn(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.warn(getUserInfo() + object);
  }

  private void warn(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    logger.warn(getUserInfo() + object, error);
  }

  private void error(String classname, String object) {
    Logger logger = LoggerFactory.getLogger(classname);
    String decodedMessage = processLogMessage(object);
    logger.error(getUserInfo() + decodedMessage);
    sendError(classname, decodedMessage, null);
  }

  private void error(String classname, String object, Throwable error) {
    Logger logger = LoggerFactory.getLogger(classname);
    String decodedMessage = processLogMessage(object);
    logger.error(getUserInfo() + decodedMessage, error);
    sendError(classname, decodedMessage, error);
  }

  private void fatal(String classname, String object) {
    error(classname, object);
  }

  private void fatal(String classname, String object, Throwable error) {
    error(classname, object, error);
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
