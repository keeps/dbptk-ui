package com.databasepreservation.common.client.models.wizard.connection;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ConnectionParameters implements Serializable {

  private String moduleName;
  private JDBCParameters jdbcParameters;
  private SSHConfiguration sshConfiguration;

  public ConnectionParameters() {
  }

  public ConnectionParameters(String moduleName, JDBCParameters connection, SSHConfiguration sshConfiguration) {
    this.moduleName = moduleName;
    this.jdbcParameters = connection;
    this.sshConfiguration = sshConfiguration;
  }

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public JDBCParameters getJdbcParameters() {
    return jdbcParameters;
  }

  public void setJdbcParameters(JDBCParameters connection) {
    this.jdbcParameters = connection;
  }

  public SSHConfiguration getSshConfiguration() {
    return sshConfiguration;
  }

  public void setSshConfiguration(SSHConfiguration sshConfiguration) {
    this.sshConfiguration = sshConfiguration;
  }

  public boolean doSSH() {
    return sshConfiguration != null;
  }

  @JsonIgnore
  public String getURLConnection() {
    StringBuilder sb = new StringBuilder();

    sb.append("jdbc:").append(moduleName).append("://");
    for (Map.Entry<String, String> entry : jdbcParameters.getConnection().entrySet()) {
      sb.append(entry.getKey()).append("=").append(entry.getValue());
    }

    return sb.toString();
  }
}
