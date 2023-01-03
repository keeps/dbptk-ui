/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.authorization;

import java.io.Serializable;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AuthorizationGroup implements Serializable {
  public enum Type {
    DEFAULT, CUSTOM
  }

  private static final long serialVersionUID = -8656197749258805638L;
  private String id;
  private String label;
  private String attributeName;
  private String attributeOperator;
  private String attributeValue;
  private Type type;

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

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
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

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
}
