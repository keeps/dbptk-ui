package com.databasepreservation.server.client.main;

import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.HTMLWidgetWrapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Footer extends Composite {

  private FlowPanel panel;
  private HTMLWidgetWrapper layout;

  public Footer() {
    super();

    panel = new FlowPanel();
    layout = new HTMLWidgetWrapper("Footer.html");

    panel.add(layout);
    initWidget(layout);
  }
}
