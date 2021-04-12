/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.wizard.connection;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class SSHConfiguration implements Serializable {

  private String hostname;
  private String port;
  private String username;
  private String password;


  public SSHConfiguration() {
  }

  public SSHConfiguration(String hostname, String port, String username, String password) {
    this.hostname = hostname;
    this.port = port;
    this.username = username;
    this.password = password;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @JsonIgnore
  public Map<String, String> getRemoteProperties() {
    Map<String, String> properties = new LinkedHashMap<>();

    properties.put("ssh", "true");
    properties.put("ssh-host", this.getHostname());
    properties.put("ssh-user", this.getUsername());
    properties.put("ssh-password", this.getPassword());
    properties.put("ssh-port", this.getPort());

    return properties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SSHConfiguration ssh = (SSHConfiguration) o;
    return getHostname().equals(ssh.getHostname()) &&
        getPort().equals(ssh.getPort()) &&
        getUsername().equals(ssh.getUsername()) &&
        getPassword().equals(ssh.getPassword());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getHostname(), getPort(), getUsername(), getPassword());
  }
}
