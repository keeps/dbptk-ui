package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AdvancedConfiguration extends ContentPanel {
  private static Map<String, AdvancedConfiguration> instances = new HashMap<>();
  private ViewerDatabase database;

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forAdvancedConfiguration(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);
  @UiField
  Label title;
  @UiField
  FlowPanel options;

  interface AdvancedConfigurationUiBinder extends UiBinder<Widget, AdvancedConfiguration> {
  }

  private static AdvancedConfigurationUiBinder binder = GWT.create(AdvancedConfigurationUiBinder.class);

  public static AdvancedConfiguration getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new AdvancedConfiguration(database));
  }

  private AdvancedConfiguration(ViewerDatabase database) {
    initWidget(binder.createAndBindUi(this));
    this.database = database;

    init();
  }

  private void init() {
    title.setText(messages.advancedConfigurationLabelForMainTitle());

    FocusPanel ManagementTablesPanel = createOptions(messages.advancedConfigurationLabelForTableManagement(),
      messages.advancedConfigurationTextForTableManagement());

    FocusPanel ManagementColumnsPanel = createOptions(messages.advancedConfigurationLabelForColumnsManagement(),
      messages.advancedConfigurationTextForColumnsManagement());

    FocusPanel DataTransformationPanel = createOptions(messages.advancedConfigurationLabelForDataTransformation(),
      messages.advancedConfigurationTextForDataTransformation());
    DataTransformationPanel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
       HistoryManager.gotoDataTransformation(database.getUuid());
      }
    });

//    options.add(ManagementTablesPanel);
//    options.add(ManagementColumnsPanel);
    options.add(DataTransformationPanel);
  }

  private FocusPanel createOptions(String title, String description) {
    FocusPanel panel = new FocusPanel();
    FlowPanel content = new FlowPanel();
    content.setStyleName("advanced-configuration-option");
    FlowPanel left = new FlowPanel();
    left.setStyleName("left");
    FlowPanel right = new FlowPanel();
    right.setStyleName("right");

    String iconTag = FontAwesomeIconManager.getTag(FontAwesomeIconManager.COG);
    HTML icon = new HTML(SafeHtmlUtils.fromSafeConstant(iconTag));
    icon.setStyleName("icon");

    Label panelTitle = new Label(title);
    panelTitle.setStyleName("title");
    Label panelDescription = new Label(description);
    panelDescription.setStyleName("description");

    left.add(icon);
    right.add(panelTitle);
    right.add(panelDescription);

    content.add(left);
    content.add(right);

    panel.add(content);
    return panel;
  }
}