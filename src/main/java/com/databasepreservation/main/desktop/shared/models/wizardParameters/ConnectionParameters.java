package com.databasepreservation.main.desktop.shared.models.wizardParameters;

import com.databasepreservation.main.desktop.shared.models.SSHConfiguration;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ConnectionParameters implements Serializable {

  private String moduleName;
  private HashMap<String, String> connection;
  private boolean ssh = false;
  private SSHConfiguration sshConfiguration;

  public ConnectionParameters() {
  }

  public ConnectionParameters(String moduleName, HashMap<String, String> connection, SSHConfiguration sshConfiguration) {
    this.moduleName = moduleName;
    this.connection = connection;
    this.sshConfiguration = sshConfiguration;
  }

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public HashMap<String, String> getConnection() {
    return connection;
  }

  public void setConnection(HashMap<String, String> connection) {
    this.connection = connection;
  }

  public SSHConfiguration getSSHConfiguration() {
    return sshConfiguration;
  }

  public void setSSHConfiguration(SSHConfiguration sshConfiguration) {
    this.sshConfiguration = sshConfiguration;
  }

  public boolean doSSH() {
    return ssh;
  }

  public void doSSH(boolean value) {
    this.ssh = value;
  }
}
