package com.databasepreservation.visualization.client.common.sidebar;

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

  protected SidebarItem() {
  }

  public SidebarItem(String text) {
    initWidget(uiBinder.createAndBindUi(this));
    setText(text);
  }

  public SidebarItem addIcon(String iconName) {
    label.addStyleName("fa-" + iconName);
    return this;
  }

  public SidebarItem setText(String text) {
    label.setText(text);
    return this;
  }

  public String getText(){
    return label.getText();
  }

  public SidebarItem setH1() {
    label.addStyleName("h1");
    return this;
  }

  public SidebarItem setH2() {
    label.addStyleName("h2");
    return this;
  }

  public SidebarItem setH3() {
    label.addStyleName("h3");
    return this;
  }

  public SidebarItem setH4() {
    label.addStyleName("h4");
    return this;
  }

  public SidebarItem setH5() {
    label.addStyleName("h5");
    return this;
  }

  public SidebarItem setH6() {
    label.addStyleName("h6");
    return this;
  }

  public SidebarItem setIndent0() {
    label.addStyleName("indent0");
    return this;
  }

  public SidebarItem setIndent1() {
    label.addStyleName("indent1");
    return this;
  }

  public SidebarItem setIndent2() {
    label.addStyleName("indent2");
    return this;
  }

  public SidebarItem setIndent3() {
    label.addStyleName("indent3");
    return this;
  }

  public SidebarItem setIndent4() {
    label.addStyleName("indent4");
    return this;
  }

  public void hide(){
    setVisible(false);
  }

  public void show(){
    setVisible(true);
  }
}
