package com.databasepreservation.common.client.models.wizard.connection;

import java.io.Serializable;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ConnectionResponse implements Serializable {
  private boolean connected;
  private String message;

  public ConnectionResponse() {
  }

  public boolean isConnected() {
    return connected;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
