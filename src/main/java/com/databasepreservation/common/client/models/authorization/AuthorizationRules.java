package com.databasepreservation.common.client.models.authorization;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AuthorizationRules implements Serializable {
  private static final long serialVersionUID = -8656197749258805638L;
  private String id;
  private String label;
  private String attributeOperator;
  private String attributeValue;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getAttributeOperator() {
    return attributeOperator;
  }

  public void setAttributeOperator(String attributeOperator) {
    this.attributeOperator = attributeOperator;
  }

  public String getAttributeValue() {
    return attributeValue;
  }

  public void setAttributeValue(String attributeValue) {
    this.attributeValue = attributeValue;
  }
}
