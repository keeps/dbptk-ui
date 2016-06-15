package com.databasepreservation.dbviewer.client.common.sidebar;

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
  public SidebarItem addIcon(String iconName) {
    label.addStyleName("fa-" + iconName);
    return this;
  }

  @Override
  public SidebarItem setText(String text) {
    label.setText(text);
    return this;
  }

  @Override
  public SidebarItem setH1() {
    label.addStyleName("h1");
    return this;
  }

  @Override
  public SidebarItem setH2() {
    label.addStyleName("h2");
    return this;
  }

  @Override
  public SidebarItem setH3() {
    label.addStyleName("h3");
    return this;
  }

  @Override
  public SidebarItem setH4() {
    label.addStyleName("h4");
    return this;
  }

  @Override
  public SidebarItem setH5() {
    label.addStyleName("h5");
    return this;
  }

  @Override
  public SidebarItem setH6() {
    label.addStyleName("h6");
    return this;
  }

  @Override
  public SidebarItem setIndent0() {
    label.addStyleName("indent0");
    return this;
  }

  @Override
  public SidebarItem setIndent1() {
    label.addStyleName("indent1");
    return this;
  }

  @Override
  public SidebarItem setIndent2() {
    label.addStyleName("indent2");
    return this;
  }

  @Override
  public SidebarItem setIndent3() {
    label.addStyleName("indent3");
    return this;
  }

  @Override
  public SidebarItem setIndent4() {
    label.addStyleName("indent4");
    return this;
  }
}
