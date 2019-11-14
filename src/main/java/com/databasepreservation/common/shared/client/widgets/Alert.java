package com.databasepreservation.common.shared.client.widgets;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */

public class Alert extends FlowPanel {
  public enum MessageAlertType {
    PRIMARY, SECONDARY, SUCCESS, DANGER, WARNING, INFO, LIGHT, DARK
  }

  public Alert() {
    super();
  }

  public Alert(MessageAlertType type, String message) {
    this(type, null, message);
  }

  private Alert(MessageAlertType type, String title, String message) {
    super();
    Label messageLabel = new Label(message);

    if(title != null){
      Label titleLabel = new Label(title);
      titleLabel.setStyleName("alert-heading");
      add(titleLabel);
    }
    add(messageLabel);

    switch (type) {
      case SECONDARY:
        setStyleName("alert alert-secondary");
        break;
      case SUCCESS:
        setStyleName("alert alert-success");
        break;
      case DANGER:
        setStyleName("alert alert-danger");
        break;
      case WARNING:
        setStyleName("alert alert-warning");
        break;
      case INFO:
        setStyleName("alert alert-info");
        break;
      case LIGHT:
        setStyleName("alert alert-light");
        break;
      case DARK:
        setStyleName("alert alert-dark");
        break;
      case PRIMARY:
      default:
        setStyleName("alert alert-primary");
        break;
    }
  }
}
