package com.databasepreservation.visualization.client.ViewerStructure;

import java.io.Serializable;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewerTrigger implements Serializable {
  private String name;
  private String actionTime;
  private String triggerEvent;
  private String aliasList;
  private String triggeredAction;
  private String description;

  public ViewerTrigger() {
  }

  public String getActionTime() {
    return actionTime;
  }

  public void setActionTime(String actionTime) {
    this.actionTime = actionTime;
  }

  public String getAliasList() {
    return aliasList;
  }

  public void setAliasList(String aliasList) {
    this.aliasList = aliasList;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTriggeredAction() {
    return triggeredAction;
  }

  public void setTriggeredAction(String triggeredAction) {
    this.triggeredAction = triggeredAction;
  }

  public String getTriggerEvent() {
    return triggerEvent;
  }

  public void setTriggerEvent(String triggerEvent) {
    this.triggerEvent = triggerEvent;
  }
}
