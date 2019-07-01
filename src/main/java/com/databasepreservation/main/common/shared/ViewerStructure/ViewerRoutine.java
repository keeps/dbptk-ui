package com.databasepreservation.main.common.shared.ViewerStructure;

import java.io.Serializable;
import java.util.List;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerRoutine implements Serializable {
  // mandatory in SIARD2
  private String name;

  // optional in SIARD2
  private String description;
  private String source;
  private String body;
  private String characteristic;
  private String returnType;
  private List<ViewerRoutineParameter> parameters;

  public ViewerRoutine() {
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getCharacteristic() {
    return characteristic;
  }

  public void setCharacteristic(String characteristic) {
    this.characteristic = characteristic;
  }

  public String getReturnType() {
    return returnType;
  }

  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  public List<ViewerRoutineParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<ViewerRoutineParameter> parameters) {
    this.parameters = parameters;
  }
}
