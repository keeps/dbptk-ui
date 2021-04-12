/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.dialogs;

import com.databasepreservation.common.client.common.NoAsyncCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

public class CommonDialogs {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public enum Level {
    DANGER, NORMAL, WARNING
  }

  public static void tablePanelOptions() {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(true);
    dialogBox.setWidth("400px");
    dialogBox.setPopupPosition(0,0);
    dialogBox.setHeight("400px");
    dialogBox.show();
  }

  public static void showConfirmDialog(String title, SafeHtml message, String cancelButtonText, String confirmButtonText,
                                       Level level, String width,
                                       final AsyncCallback<Boolean> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    FlowPanel layout = new FlowPanel();

    HTML messageLabel = new HTML(message);
    Button cancelButton = new Button(cancelButtonText);
    Button confirmButton = new Button(confirmButtonText);
    FlowPanel footer = new FlowPanel();

    layout.add(messageLabel);
    layout.add(footer);
    footer.add(cancelButton);
    footer.add(confirmButton);

    dialogBox.setWidget(layout);
    dialogBox.setWidth(width);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    cancelButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onSuccess(false);
    });

    confirmButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onSuccess(true);
    });

    switch (level) {
      case WARNING:
        dialogBox.addStyleName("wui-dialog-confirm");
        layout.addStyleName("wui-dialog-layout");
        footer.addStyleName("wui-dialog-layout-footer");
        messageLabel.addStyleName("wui-dialog-message");
        cancelButton.addStyleName("btn btn-link");
        confirmButton.addStyleName("btn btn-primary");
        break;
      case DANGER:
        dialogBox.addStyleName("dialog-persist-errors");
        layout.addStyleName("dialog-persist-errors-layout");
        footer.addStyleName("dialog-persist-errors-layout-footer");
        messageLabel.addStyleName("wui-dialog-message");
        cancelButton.addStyleName("btn btn-link");
        confirmButton.addStyleName("btn btn-danger btn-delete");
        break;
      case NORMAL:
      default:
        dialogBox.addStyleName("wui-dialog-confirm");
        layout.addStyleName("wui-dialog-layout");
        footer.addStyleName("wui-dialog-layout-footer");
        messageLabel.addStyleName("wui-dialog-message");
        cancelButton.addStyleName("btn btn-link");
        confirmButton.addStyleName("btn btn-play");
        break;
    }

    dialogBox.center();
    dialogBox.show();
  }

  public static void showInformationDialog(String title, String message, String continueButtonText) {
    showInformationDialog(title, message, continueButtonText, new NoAsyncCallback<Void>());
  }

  public static void showInformationDialog(String title, String message, String continueButtonText,
    final AsyncCallback<Void> callback) {
    final DialogBox dialogBox = new DialogBox(false, true);
    dialogBox.setText(title);

    FlowPanel layout = new FlowPanel();
    Label messageLabel = new Label(message);
    Button continueButton = new Button(continueButtonText);

    layout.add(messageLabel);
    layout.add(continueButton);

    dialogBox.setWidget(layout);

    dialogBox.setGlassEnabled(true);
    dialogBox.setAnimationEnabled(false);

    continueButton.addClickHandler(event -> {
      dialogBox.hide();
      callback.onSuccess(null);
    });

    dialogBox.addStyleName("wui-dialog-information");
    layout.addStyleName("wui-dialog-layout");
    messageLabel.addStyleName("wui-dialog-message");
    continueButton.addStyleName("btn btn-play");

    dialogBox.center();
    dialogBox.show();
  }
}
