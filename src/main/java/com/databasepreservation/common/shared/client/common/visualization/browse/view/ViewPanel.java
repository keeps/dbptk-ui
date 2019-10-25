package com.databasepreservation.common.shared.client.common.visualization.browse.view;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.common.shared.ViewerStructure.ViewerView;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.common.MetadataField;
import com.databasepreservation.common.shared.client.common.RightPanel;
import com.databasepreservation.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.common.shared.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.common.shared.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.rabbitmq.client.AMQP;
import config.i18n.client.ClientMessages;
import dk.sa.xmlns.diark._1_0.fileindex.FileIndexType;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ViewPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, ViewPanel> instances = new HashMap<>();

  /**
   * Uses BreadcrumbManager to show available information in the breadcrumbPanel
   *
   * @param breadcrumb
   *          the BreadcrumbPanel for this database
   */
  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {

      BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forTable(database.getMetadata().getName(),
        database.getUUID(), view.getName(), view.getUUID()));

  }

  interface ViewPanelUiBinder extends UiBinder<Widget, ViewPanel> {
  }

  private static ViewPanelUiBinder uiBinder = GWT.create(ViewPanelUiBinder.class);
  private ViewerDatabase database;
  private ViewerView view;

  @UiField
  SimplePanel mainHeader;

  @UiField
  FlowPanel content;

  @UiField
  FlowPanel description;

  @UiField
  Button options;

  public static ViewPanel getInstance(ViewerDatabase database, String viewUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + viewUUID;

    ViewPanel instance = instances.get(code);
    if (instance == null) {
      instance = new ViewPanel(database, viewUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  private ViewPanel(ViewerDatabase database, String viewUUID) {
    this.database = database;
    this.view = database.getMetadata().getView(viewUUID);

    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  private void init() {
    mainHeader.setWidget(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_VIEWS),
      view.getName(), "h1"));

    options.setText(messages.schemaStructurePanelTextForAdvancedOption());

    options.addClickHandler(event -> HistoryManager.gotoViewOptions(database.getUUID(), view.getUUID()));

    if (ViewerStringUtils.isNotBlank(view.getDescription())) {
      MetadataField instance = MetadataField.createInstance(view.getDescription());
      instance.setCSS("table-row-description");
      description.add(instance);
    }

    content.add(CommonClientUtils.getPanelInformation(messages.viewPanelInformationLabel(), messages.viewPanelViewerNotMaterialized(), "metadata-information-element-value"));

  }
}
