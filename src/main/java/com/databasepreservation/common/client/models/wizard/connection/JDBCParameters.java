/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.wizard.connection;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JDBCParameters implements Serializable {

  private Map<String, String> connection;
  private boolean driver = false;
  private String driverPath;

  public JDBCParameters() {}

  public JDBCParameters(Map<String, String> connection, boolean driver, String driverPath) {
    this.connection = connection;
    this.driver = driver;
    this.driverPath = driverPath;
  }

  public Map<String, String> getConnection() {
    return connection;
  }

  public void setConnection(Map<String, String> connection) {
    this.connection = connection;
  }

  public boolean isDriver() {
    return driver;
  }

  public void setDriver(boolean driver) {
    this.driver = driver;
  }

  public String getDriverPath() {
    return driverPath;
  }

  public void setDriverPath(String driverPath) {
    this.driverPath = driverPath;
  }
}

