package com.databasepreservation.common.shared.client.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class NavigationPanel extends Composite {
  interface navigationPanelUiBinder extends UiBinder<Widget, NavigationPanel> {
  }

  private static navigationPanelUiBinder binder = GWT.create(navigationPanelUiBinder.class);

  @UiField
  FlowPanel navigationPanelHeader, navigationPanelInfo, navigationPanelOptions;

  public static NavigationPanel createInstance(String title) {
    return new NavigationPanel(title);
  }

  private NavigationPanel(String title) {
    initWidget(binder.createAndBindUi(this));

    Label l = new Label();
    l.setText(title);
    navigationPanelHeader.add(l);
  }

  public void addToInfoPanel(Widget widget) {
    this.navigationPanelInfo.add(widget);
  }

  public void addButton(Button button) {
    this.navigationPanelOptions.add(button);
  }
}