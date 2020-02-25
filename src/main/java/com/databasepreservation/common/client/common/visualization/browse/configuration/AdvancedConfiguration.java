package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.NavigationPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AdvancedConfiguration extends ContentPanel {
  private ClientMessages messages = GWT.create(ClientMessages.class);

  interface AdvancedConfigurationUiBinder extends UiBinder<Widget, AdvancedConfiguration> {
  }

  private static AdvancedConfigurationUiBinder binder = GWT.create(AdvancedConfigurationUiBinder.class);

  @UiField
  FlowPanel header, content;

  @UiField
  SimplePanel description;

  private static Map<String, AdvancedConfiguration> instances = new HashMap<>();
  private ViewerDatabase database;

  public static AdvancedConfiguration getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new AdvancedConfiguration(database));
  }

  private AdvancedConfiguration(ViewerDatabase database) {
    initWidget(binder.createAndBindUi(this));
    this.database = database;

    init();
  }

  private void init() {
    configureHeader();

    NavigationPanel tableManagement = NavigationPanel
      .createInstance(messages.advancedConfigurationLabelForTableManagement());
    tableManagement.addToDescriptionPanel(messages.advancedConfigurationTextForTableManagement());
    Button btnTableManagement = new Button(messages.advancedConfigurationBtnForTableManagement());
    btnTableManagement.setStyleName("btn btn-outline-primary btn-play btn-block");
    btnTableManagement.addClickHandler(event -> {
      HistoryManager.gotoTableManagement(database.getUuid());
    });
    tableManagement.addButton(btnTableManagement);
    content.add(tableManagement);

    NavigationPanel columnManagement = NavigationPanel
      .createInstance(messages.advancedConfigurationLabelForColumnsManagement());
    columnManagement.addToDescriptionPanel(messages.advancedConfigurationTextForColumnsManagement());
    Button btnColumnManagement = new Button(messages.advancedConfigurationBtnForColumnsManagement());
    btnColumnManagement.setStyleName("btn btn-outline-primary btn-play btn-block");
    btnColumnManagement.addClickHandler(event -> {
      HistoryManager.gotoColumnsManagement(database.getUuid());
    });
    columnManagement.addButton(btnColumnManagement);
    content.add(columnManagement);

    NavigationPanel dataTransformation = NavigationPanel
      .createInstance(messages.advancedConfigurationLabelForDataTransformation());
    Button btnDataTransformation = new Button(messages.advancedConfigurationBtnForDataTransformation());

    String style;
    SafeHtml messageDescription;
    if (ApplicationType.getType().equals(ViewerConstants.SERVER)) {
      messageDescription = messages.advancedConfigurationTextForDataTransformationServer();
      style = "btn btn-outline-primary btn-play btn-block";
      btnDataTransformation.addClickHandler(event -> {
        HistoryManager.gotoDataTransformation(database.getUuid());
      });
    } else {
      messageDescription = messages.advancedConfigurationTextForDataTransformationDesktop();
      style = "btn btn-play btn-block";
      btnDataTransformation.setEnabled(false);
    }
    dataTransformation.addToDescriptionPanel(messageDescription);
    btnDataTransformation.setStyleName(style);
    dataTransformation.addButton(btnDataTransformation);
    content.add(dataTransformation);
  }

  private void configureHeader() {
    header.add(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SLIDERS),
      messages.advancedConfigurationLabelForMainTitle(), "h1"));

    HTML html = new HTML(messages.advancedConfigurationPageDescription());
    html.addStyleName("font-size-description advanced-configuration-description");

    description.setWidget(html);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forAdvancedConfiguration(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }
}