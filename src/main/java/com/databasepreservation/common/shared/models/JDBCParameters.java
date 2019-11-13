package com.databasepreservation.common.shared.models;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JDBCParameters implements Serializable {

  private HashMap<String, String> connection;
  private boolean driver = false;
  private String driverPath;
  private boolean shouldCountRows = true;

  public JDBCParameters() {}

  public JDBCParameters(HashMap<String, String> connection, boolean driver, String driverPath) {
    this.connection = connection;
    this.driver = driver;
    this.driverPath = driverPath;
  }

  public HashMap<String, String> getConnection() {
    return connection;
  }

  public void setConnection(HashMap<String, String> connection) {
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

