package com.databasepreservation.main.common.shared.ViewerStructure;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerRoleStructure implements IsSerializable {
  // mandatory in SIARD2
  private String name;
  private String admin;

  // optional in SIARD2
  private String description;

  public ViewerRoleStructure() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAdmin() {
    return admin;
  }

  public void setAdmin(String admin) {
    this.admin = admin;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
