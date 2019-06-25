package com.databasepreservation.main.visualization.client.common.sidebar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SidebarHyperlink extends SidebarItem {
  interface SidebarHyperlinkUiBinder extends UiBinder<Widget, SidebarHyperlink> {
  }

  private static SidebarHyperlinkUiBinder uiBinder = GWT.create(SidebarHyperlinkUiBinder.class);

  @UiField
  Hyperlink label;

  @Override
  protected Widget getLabelAsWidget() {
    return label;
  }

  public SidebarHyperlink(String text, String targetHistoryToken) {
    initWidget(uiBinder.createAndBindUi(this));
    setTargetHistoryToken(targetHistoryToken);
    setText(text);
    label.addStyleName("sidebar-hyperlink");
  }

  public SidebarHyperlink setTargetHistoryToken(String targetHistoryToken) {
    label.setTargetHistoryToken(targetHistoryToken);
    return this;
  }

  @Override
  public SidebarItem setText(String text) {
    label.setText(text);
    return this;
  }

  @Override
  public String getText() {
    return label.getText();
  }
}
