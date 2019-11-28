package com.databasepreservation.common.client.common.sidebar;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SidebarHyperlink extends SidebarItem {
  private final static String CSS_ITEM_SELECTED = "sidebarItem-selected";
  interface SidebarHyperlinkUiBinder extends UiBinder<Widget, SidebarHyperlink> {
  }

  private static SidebarHyperlinkUiBinder uiBinder = GWT.create(SidebarHyperlinkUiBinder.class);

  @UiField
  Hyperlink label;

  @UiField
  FlowPanel container;

  private Hyperlink hyperlink = null;

  @Override
  protected Widget getLabelAsWidget() {
    return label;
  }

  public SidebarHyperlink(String text, String targetHistoryToken) {
    initWidget(uiBinder.createAndBindUi(this));
    setTargetHistoryToken(targetHistoryToken);
    setText(text);
    label.addStyleName("sidebar-hyperlink sidebarItem");
  }

  public SidebarHyperlink(SafeHtml text, String targetHistoryToken) {
    initWidget(uiBinder.createAndBindUi(this));
    setTargetHistoryToken(targetHistoryToken);
    setTextBySafeHTML(text);
    label.addStyleName("sidebar-hyperlink sidebarItem");
  }

  public SidebarHyperlink(String text, String targetHistoryToken, Hyperlink hyperlink) {
    initWidget(uiBinder.createAndBindUi(this));
    setTargetHistoryToken(targetHistoryToken);
    setText(text);
    label.addStyleName("sidebar-hyperlink sidebarItem");
    this.hyperlink = hyperlink;
    container.add(this.hyperlink);
  }

  public SidebarHyperlink(SafeHtml text, String targetHistoryToken, Hyperlink hyperlink) {
    initWidget(uiBinder.createAndBindUi(this));
    setTargetHistoryToken(targetHistoryToken);
    setTextBySafeHTML(text);
    label.addStyleName("sidebar-hyperlink sidebarItem");
    this.hyperlink = hyperlink;
    container.add(this.hyperlink);
  }

  public SidebarHyperlink setTooltip(final String tooltip) {
    label.setTitle(tooltip);
    return this;
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
  public SidebarItem setTextBySafeHTML(SafeHtml safeHtml) {
    label.setHTML(safeHtml);
    return this;
  }

  @Override
  public String getText() {
    return label.getText();
  }

  public void setSelected(boolean value) {
    if (value) {
      container.addStyleName("sidebarHyperLink-selected");
      label.addStyleName(CSS_ITEM_SELECTED);
      if (hyperlink != null) {
        hyperlink.addStyleName(CSS_ITEM_SELECTED);
      }
    } else {
      container.removeStyleName("sidebarHyperLink-selected");
      label.removeStyleName(CSS_ITEM_SELECTED);
      if (hyperlink != null) {
        hyperlink.removeStyleName(CSS_ITEM_SELECTED);
      }
    }
  }
}
