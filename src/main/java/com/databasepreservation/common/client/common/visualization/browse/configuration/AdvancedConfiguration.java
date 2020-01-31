package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
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

    Button btnTableManagement = new Button();
    btnTableManagement.addClickHandler(event -> {
      HistoryManager.gotoTableManagement(database.getUuid());
    });
    content.add(createCard(FontAwesomeIconManager.TABLE, messages.advancedConfigurationLabelForTableManagement(), btnTableManagement));

    Button btnColumnManagement = new Button();
    btnColumnManagement.addClickHandler(event -> {
      HistoryManager.gotoColumnsManagement(database.getUuid());
    });
    content.add(createCard(FontAwesomeIconManager.COLUMN, messages.advancedConfigurationLabelForColumnsManagement(), btnColumnManagement));

    if (ApplicationType.getType().equals(ViewerConstants.SERVER)) {
      Button btnDataTransformation = new Button();
      btnDataTransformation.addClickHandler(event -> {
        HistoryManager.gotoDataTransformation(database.getUuid());
      });
      content.add(createCard(FontAwesomeIconManager.DATA_TRANSFORMATION, messages.advancedConfigurationLabelForDataTransformation(), btnDataTransformation));
    }

  }

  private void configureHeader() {
    header.add(CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SLIDERS),
      messages.advancedConfigurationLabelForMainTitle(), "h1"));
  }

  private FlowPanel createCard(String icon, String title, Button button){
    FlowPanel headerPanel = CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(icon),
        title, "h3");
    button.setStyleName("btn btn-play");
    FlowPanel footerPanel = new FlowPanel();
    footerPanel.add(button);

    return CommonClientUtils.wrapOnDiv("navigation-card", headerPanel, footerPanel);
  }
  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forAdvancedConfiguration(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }
}