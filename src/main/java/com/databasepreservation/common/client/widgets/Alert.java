package com.databasepreservation.common.client.widgets;

import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
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
    this(type, null, message, false);
  }

  public Alert(MessageAlertType type, String message, String icon) {
    this(type, null, message, false, icon);
  }

  public Alert(MessageAlertType type, String message, boolean dismissible) {
    this(type, null, message, dismissible);
  }

  public Alert(MessageAlertType type, String message, boolean dismissible, String icon) {
    this(type, null, message, dismissible, icon);
  }

  private Alert(MessageAlertType type, String title, String message, boolean dismissible) {
    this(type, title, message, dismissible, null);
  }

  private Alert(MessageAlertType type, String title, String message, boolean dismissible, String iconTag) {
    super();
    Label messageLabel = new Label(message);

    if(iconTag != null){
      HTML icon = new HTML(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(iconTag)));
      icon.setStyleName("alert-icon");
      add(icon);
    }

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

    if (dismissible) {
      Button button = new Button();
      button.addStyleName("close");
      SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
      safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant("<span>")).appendHtmlConstant("&times").appendHtmlConstant("</span>");
      button.setHTML(safeHtmlBuilder.toSafeHtml());
      button.addClickHandler(event -> {
        this.setVisible(false);
      });
      add(button);
    }
  }
}