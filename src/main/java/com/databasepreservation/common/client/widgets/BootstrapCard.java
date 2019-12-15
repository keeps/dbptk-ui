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
  private FlowPanel card, body, extraContent;
  private Label title;
  private HTML titleIcon;
  private Label description;
  private String uuid;

  public BootstrapCard() {
    super();
    setStyleName("bootstrap-card");
    card = new FlowPanel();
    body = new FlowPanel();
    extraContent = new FlowPanel();
    title = new Label();
    titleIcon = new HTML();
    description = new Label();
    titleIcon.setStyleName("bootstrap-card-title-icon");
    title.setStyleName("bootstrap-card-title");
    description.setStyleName("bootstrap-card-description");

    FlowPanel titlePanel = new FlowPanel();
    titlePanel.add(titleIcon);
    titlePanel.add(title);
    titlePanel.setStyleName("bootstrap-card-title-panel");

    body.add(titlePanel);
    body.add(description);
    body.setStyleName("bootstrap-card-body");
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
    this.title.setText(title);
  }

  public void setTitleIcon(String icon){
    this.titleIcon.setHTML(SafeHtmlUtils.fromSafeConstant(icon));
  }

  public void setDescription(String description) {
    this.description.setText(description);
  }

  public void addExtraContent(FlowPanel content){
    FlowPanel extraContent = content;
    extraContent.setStyleName("bootstrap-card-extra-content");
    body.add(extraContent);
  }

  public void addHideContent(FlowPanel content, FlowPanel action){
    extraContent = content;
    extraContent.setStyleName("bootstrap-card-extra-content");
    body.add(extraContent);

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
