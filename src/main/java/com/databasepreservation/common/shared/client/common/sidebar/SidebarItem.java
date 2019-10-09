package com.databasepreservation.common.shared.client.common.sidebar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SidebarItem extends Composite {
  interface SidebarItemUiBinder extends UiBinder<Widget, SidebarItem> {
  }

  private static SidebarItemUiBinder uiBinder = GWT.create(SidebarItemUiBinder.class);

  @UiField
  Label label;

  protected Widget getLabelAsWidget() {
    return label;
  }

  protected SidebarItem() {
  }

  public SidebarItem(String text) {
    initWidget(uiBinder.createAndBindUi(this));
    setText(text);
  }

  public SidebarItem addIcon(String iconName) {
    getLabelAsWidget().addStyleName("fa-" + iconName);
    return this;
  }

  public SidebarItem setText(String text) {
    label.setText(text);
    return this;
  }

  public String getText() {
    return label.getText();
  }

  public SidebarItem setH1() {
    getLabelAsWidget().addStyleName("h1");
    return this;
  }

  public SidebarItem setH2() {
    getLabelAsWidget().addStyleName("h2");
    return this;
  }

  public SidebarItem setH3() {
    getLabelAsWidget().addStyleName("h3");
    return this;
  }

  public SidebarItem setH4() {
    getLabelAsWidget().addStyleName("h4");
    return this;
  }

  public SidebarItem setH5() {
    getLabelAsWidget().addStyleName("h5");
    return this;
  }

  public SidebarItem setH6() {
    getLabelAsWidget().addStyleName("h6");
    return this;
  }

  public SidebarItem setIndent0() {
    getLabelAsWidget().addStyleName("indent0");
    return this;
  }

  public SidebarItem setIndent1() {
    getLabelAsWidget().addStyleName("indent1");
    return this;
  }

  public SidebarItem setIndent2() {
    getLabelAsWidget().addStyleName("indent2");
    return this;
  }

  public SidebarItem setIndent3() {
    getLabelAsWidget().addStyleName("indent3");
    return this;
  }

  public SidebarItem setIndent4() {
    getLabelAsWidget().addStyleName("indent4");
    return this;
  }

  public int getIndent() {
    String styles = getLabelAsWidget().getStyleName();
    int indentIndex = styles.indexOf("indent") + 6;
    try {
      return Integer.valueOf(styles.substring(indentIndex, indentIndex + 1));
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}
