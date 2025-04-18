/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.structure;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerPrivilegeStructure implements Serializable {
  @Serial
  private static final long serialVersionUID = 1608057895274168513L;
  // mandatory in SIARD2
  private String type;
  private String grantor;
  private String grantee;

  // Optional in SIARD2
  private String object;
  private String option;
  private String description;

  public ViewerPrivilegeStructure() {
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getGrantee() {
    return grantee;
  }

  public void setGrantee(String grantee) {
    this.grantee = grantee;
  }

  public String getGrantor() {
    return grantor;
  }

  public void setGrantor(String grantor) {
    this.grantor = grantor;
  }

  public String getObject() {
    return object;
  }

  public void setObject(String object) {
    this.object = object;
  }

  public String getOption() {
    return option;
  }

  public void setOption(String option) {
    this.option = option;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
