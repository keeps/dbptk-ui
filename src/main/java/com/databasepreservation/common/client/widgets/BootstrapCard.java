/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BootstrapCard extends FocusPanel {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private FlowPanel card = new FlowPanel();
  private FlowPanel titlePanel = new FlowPanel();
  private FlowPanel body = new FlowPanel();
  private FlowPanel extraContent = new FlowPanel();
  private Label title = new Label();
  private HTML titleIcon = new HTML();
  private Label description = new Label();
  private String uuid;

  public BootstrapCard() {
    super();
    setStyleName("bootstrap-card");
    description.setStyleName("bootstrap-card-description");
    titlePanel.add(titleIcon);
    titlePanel.add(title);
    body.add(titlePanel);
    body.add(description);
    card.add(body);
    add(card);
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public void setTitle(String title){
    this.title.setStyleName("bootstrap-card-title");
    this.title.setText(title);
    this.titlePanel.setStyleName("bootstrap-card-title-panel");
  }

  public void setTitleIcon(String icon){
    titleIcon.setStyleName("bootstrap-card-title-icon");
    titleIcon.setHTML(SafeHtmlUtils.fromSafeConstant(icon));
  }

  public void setDescription(String description) {
    this.description.setText(description);
  }

  public void addExtraContent(FlowPanel content){
    FlowPanel extraContent = content;
    extraContent.setStyleName("bootstrap-card-extra-content");
    body.add(extraContent);
    body.setStyleName("bootstrap-card-body");
  }

  public void addHideContent(FlowPanel content, FlowPanel action){
    extraContent = content;
    extraContent.setStyleName("bootstrap-card-extra-content");
    body.add(extraContent);
    body.setStyleName("bootstrap-card-body");

    action.addStyleName("bootstrap-card-extra-content-btn");
    card.add(action);
  }

  public void setHideContentVisible(Boolean enable){
    extraContent.setVisible(enable);
  }

  public void setFooter(FlowPanel footer){
    footer.setStyleName("bootstrap-card-footer");
    card.add(footer);
  }
}
