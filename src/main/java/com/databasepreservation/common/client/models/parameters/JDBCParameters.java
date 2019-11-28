package com.databasepreservation.common.client.models.parameters;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JDBCParameters implements Serializable {

  private Map<String, String> connection;
  private boolean driver = false;
  private String driverPath;
  private boolean shouldCountRows = true;

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

  public void shouldCountRows(boolean shouldCountRows) {
    this.shouldCountRows = shouldCountRows;
  }

  public boolean shouldCountRows() {
    return shouldCountRows;
  }
}

