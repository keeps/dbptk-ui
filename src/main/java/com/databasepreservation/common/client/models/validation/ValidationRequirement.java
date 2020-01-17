package com.databasepreservation.common.client.models.validation;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ValidationRequirement implements Serializable {
  private String ID;
  private String message;
  private String status;
  private ValidationRequirement.Type type;

  public ValidationRequirement(ValidationRequirement.Type type) {
    this.type = type;
  }

  public ValidationRequirement() {
  }

  public ValidationRequirement.Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public enum Type {
    REQUIREMENT, REQUIREMENT_INIT, MESSAGE, SUB_REQUIREMENT, ADDITIONAL, PATH, SPARSE_PROGRESS, PATH_COMPLETE
  }

  public void setId(String ID) {
    this.ID = ID;
  }

  public String getId() {
    return ID;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
