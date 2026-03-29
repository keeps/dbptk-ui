/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.dataTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.api.v1.utils.ConfigurationContext;
import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.LoadingDiv;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.sidebar.DataTransformationSidebar;
import com.databasepreservation.common.client.common.visualization.browse.configuration.ConfigurationStatusPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.handler.DataTransformationUtils;
import com.databasepreservation.common.client.common.visualization.browse.information.ErDiagram;
import com.databasepreservation.common.client.configuration.observer.ICollectionStatusObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.denormalization.DenormalizeConfiguration;
import com.databasepreservation.common.client.models.status.denormalization.RelatedTablesConfiguration;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerForeignKey;
import com.databasepreservation.common.client.models.structure.ViewerJobStatus;
import com.databasepreservation.common.client.models.structure.ViewerReference;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.BootstrapCard;
import com.databasepreservation.common.client.widgets.SwitchBtn;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class DataTransformation extends RightPanel implements ICollectionStatusObserver {

  interface DataTransformerUiBinder extends UiBinder<Widget, DataTransformation> {
  }

  private static DataTransformerUiBinder binder = GWT.create(DataTransformerUiBinder.class);
  private static Map<String, DataTransformation> instances = new HashMap<>();
  private static Map<String, DenormalizeConfiguration> denormalizeConfigurationList = new HashMap<>();

  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel content;

  @UiField
  SimplePanel message;

  @UiField
  FlowPanel toolBar;

  @UiField
  FlowPanel rootTablePanel;

  @UiField
  LoadingDiv loading;

  @UiField
  ConfigurationStatusPanel configurationStatusPanel;

  private ViewerDatabase database;
  private ViewerTable table;
  private String tableId;
  private TransformationTable rootTable;
  private DataTransformationSidebar sidebar;
  private CollectionStatus collectionStatus;
  private DenormalizeConfiguration denormalizeConfiguration;
  private Button btnSaveConfiguration = new Button();
  private Button btnGotoTable = new Button();
  private Button btnCancel = new Button();
  private List<Button> buttons = new ArrayList<>();
  private boolean isInformation;

  /** Flag to manage state synchronization with the backend projection. */
  private boolean isInitialized = false;

  /**
   * Tracks dynamically added widgets to allow surgical clearing without killing
   * UiBinder nodes.
   */
  private List<Widget> dynamicWidgets = new ArrayList<>();

  public static DataTransformation getInstance(CollectionStatus collectionStatus, ViewerDatabase database,
    DataTransformationSidebar sidebar) {
    return getInstance(collectionStatus, database, null, sidebar);
  }

  public static DataTransformation getInstance(CollectionStatus collectionStatus, ViewerDatabase database,
    String tableId, DataTransformationSidebar sidebar) {
    String key = database.getUuid() + (tableId == null ? "info" : tableId);
    DataTransformation instance = instances.computeIfAbsent(key,
      k -> new DataTransformation(collectionStatus, database, tableId, sidebar));
    instance.sidebar = sidebar;
    return instance;
  }

  private DataTransformation(CollectionStatus collectionStatus, ViewerDatabase database, String tableId,
    DataTransformationSidebar sidebar) {

    initWidget(binder.createAndBindUi(this));
    ObserverManager.getCollectionObserver().addObserver(this);
    this.database = database;
    this.sidebar = sidebar;
    this.collectionStatus = collectionStatus;
    this.tableId = tableId;
    this.isInitialized = false;

    this.configurationStatusPanel.setDatabase(this.database);

    if (this.tableId == null) {
      this.isInformation = true;
      this.toolBar.setVisible(false);
    } else {
      this.isInformation = false;
    }
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    if (!isInitialized) {
      fetchProjectedDatabase();
    } else if (database != null) {
      updateControllerPanel();
    }
  }

  @Override
  public void updateCollection(CollectionStatus newStatus) {
    if (this.collectionStatus != null && this.collectionStatus.getDatabaseUUID().equals(newStatus.getDatabaseUUID())) {
      if (this.collectionStatus != newStatus) {
        this.isInitialized = false;
        if (this.isAttached()) {
          fetchProjectedDatabase();
        }
      }
    }
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forDataTransformation(database.getUuid(),
      database.getMetadata().getName(), messages.breadcrumbTextForDataTransformation());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  private void fetchProjectedDatabase() {
    loading.setVisible(true);

    CollectionService.Util.call((ConfigurationContext context) -> {
      if (context != null && context.getProjectedDatabase() != null) {
        this.database = context.getProjectedDatabase();
        this.collectionStatus = context.getCollectionStatus();
        this.isInitialized = true;

        configurationStatusPanel.setDatabase(this.database);

        if (this.sidebar != null) {
          this.sidebar.reset(this.database, this.collectionStatus);
        }

        rebuildDynamicUI();
      }
    }).getConfigurationContext(this.database.getUuid(), this.database.getUuid());
  }

  /**
   * Surgical rebuild: Only clears and re-adds widgets in the dynamic containers.
   * This prevents the ConfigurationStatusPanel from being removed from the DOM.
   */
  private void rebuildDynamicUI() {
    // Clear only the widgets we manually added, leaving UiBinder static nodes
    // intact
    for (Widget w : dynamicWidgets) {
      w.removeFromParent();
    }
    dynamicWidgets.clear();
    message.clear();
    rootTablePanel.clear();

    if (isInformation) {
      Widget diagram = informationPanel();
      rootTablePanel.add(diagram);
      dynamicWidgets.add(diagram);
      loading.setVisible(false);
    } else {
      getDenormalizeConfigurationFile(this.tableId);
    }
  }

  private void createToolBar() {
    toolBar.clear();
    FlowPanel tablePanel = new FlowPanel();
    HTML icon = new HTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE));
    icon.addStyleName("data-transformation-title-icon");
    Label tableName = new Label(table.getId());
    tableName.addStyleName("data-transformation-title-label");
    tablePanel.add(icon);
    tablePanel.add(tableName);
    tablePanel.setStyleName("data-transformation-title");

    FlowPanel panel = new FlowPanel();
    panel.addStyleName("data-transformation-toolbar-actions");
    panel.add(btnGotoTable);

    toolBar.add(tablePanel);
    toolBar.add(panel);
    toolBar.setVisible(true);
  }

  private ErDiagram informationPanel() {
    return ErDiagram.getInstance(database, database.getMetadata().getSchemas().get(0),
      HistoryManager.ROUTE_DATA_TRANSFORMATION, collectionStatus);
  }

  private void getDenormalizeConfigurationFile(String tableId) {
    this.table = database.getMetadata().getTableById(tableId);

    if (this.table == null) {
      HistoryManager.gotoAdvancedConfiguration(database.getUuid());
      return;
    }

    if (collectionStatus.getDenormalizations()
      .contains(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + table.getUuid())) {
      CollectionService.Util.call((DenormalizeConfiguration response) -> {
        denormalizeConfiguration = response;
        initDataTransformationUI();
      }).getDenormalizeConfigurationFile(database.getUuid(), database.getUuid(), table.getUuid());
    } else {
      denormalizeConfiguration = new DenormalizeConfiguration(database.getUuid(), table);
      initDataTransformationUI();
    }
  }

  private void initDataTransformationUI() {
    denormalizeConfigurationList.put(table.getUuid(), denormalizeConfiguration);
    loading.setVisible(false);

    Alert alert = new Alert(Alert.MessageAlertType.INFO, messages.dataTransformationTextForAlertColumnsOrder(), true,
      FontAwesomeIconManager.DATABASE_INFORMATION);
    message.setWidget(alert);

    TableNode parentNode = new TableNode(database, table, collectionStatus);
    parentNode.setUuid(table.getUuid());
    parentNode.setupChildren();

    Widget rootCard = createRootTableCard(parentNode.getTable());
    rootTablePanel.add(rootCard);
    dynamicWidgets.add(rootCard);

    FlowPanel relationShipPanel = new FlowPanel();
    relationShipPanel.setStyleName("data-transformation-panel");
    relationShipPanel.add(expandLevel(parentNode));
    rootTablePanel.add(relationShipPanel);
    dynamicWidgets.add(relationShipPanel);

    setupActionButtons();
    createToolBar();
  }

  private void setupActionButtons() {
    buttons.clear();
    btnCancel.setEnabled(false);
    btnCancel.setText(messages.basicActionCancel());
    btnCancel.addStyleName("btn btn-times-circle btn-danger");
    btnCancel.addClickHandler(clickEvent -> {
      if (sidebar != null) {
        sidebar.reset(database, collectionStatus);
      }
      HistoryManager.gotoAdvancedConfiguration(database.getUuid());
    });

    btnGotoTable.setText(messages.dataTransformationBtnBrowseTable());
    btnGotoTable.setStyleName("btn btn-table");
    btnGotoTable.addClickHandler(event -> HistoryManager.gotoTable(database.getUuid(), tableId));

    btnSaveConfiguration.setText(messages.basicActionSave());
    btnSaveConfiguration.setStyleName("btn btn-save");
    btnSaveConfiguration
      .addClickHandler(clickEvent -> saveConfiguration(database.getUuid(), denormalizeConfigurationList.entrySet()));

    buttons.add(btnCancel);
    buttons.add(btnSaveConfiguration);

    updateControllerPanel();
  }

  private BootstrapCard createRootTableCard(ViewerTable table) {
    BootstrapCard card = new BootstrapCard();
    card.setDescription(table.getDescription());
    rootTable = TransformationTable.getInstance(database, table, denormalizeConfiguration, collectionStatus);
    FlowPanel rootTablePanel = new FlowPanel();
    rootTablePanel.add(rootTable);
    card.addExtraContent(rootTablePanel);
    return card;
  }

  private FlowPanel createChildTableCard(TableNode childNode) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("data-transformation-wrapper");

    FlowPanel grandChild = new FlowPanel();
    grandChild.addStyleName("data-transformation-child");

    ViewerTable childTable = childNode.getTable();
    BootstrapCard card = new BootstrapCard();

    card.setTitleIcon(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE));

    String cardTitle = childTable.getName();
    if (collectionStatus.getTableStatusByTableId(childTable.getId()) != null) {
      String customName = collectionStatus.getTableStatusByTableId(childTable.getId()).getCustomName();
      if (customName != null && !customName.trim().isEmpty())
        cardTitle = customName;
    }
    card.setTitle(cardTitle);
    card.setDescription(childTable.getDescription());
    card.addStyleName("card-disabled");
    card.addExtraContent(getInformationAboutRelationship(childNode));
    card.getElement().setId(childNode.getUuid());

    if (childNode.getIsVirtual())
      card.addStyleName("card-virtual");

    FlowPanel container = new FlowPanel();
    TransformationChildTables tableInstance = TransformationChildTables.createInstance(childNode,
      denormalizeConfiguration, rootTable, buttons);
    MultipleSelectionTablePanel<ViewerColumn> selectTable = tableInstance.createTable();

    SwitchBtn switchBtn = new SwitchBtn("Enable", false);
    switchBtn.setClickHandler(event -> {
      switchBtn.getButton().setValue(!switchBtn.getButton().getValue(), true);
      if (switchBtn.getButton().getValue()) {
        card.removeStyleName("card-disabled");
        grandChild.add(expandLevel(childNode));
        DataTransformationUtils.includeRelatedTable(childNode, denormalizeConfiguration, collectionStatus);
        container.add(selectTable);
      } else {
        DataTransformationUtils.removeRelatedTable(childNode, denormalizeConfiguration);
        card.addStyleName("card-disabled");
        grandChild.clear();
        container.clear();
        selectTable.getSelectionModel().clear();
        rootTable.redrawTable(denormalizeConfiguration);
        buttons.forEach(button -> button.setEnabled(true));
      }
    });

    FlowPanel switchPanel = new FlowPanel();
    switchPanel.add(switchBtn);
    card.addHideContent(container, switchPanel);

    panel.add(card);
    panel.add(grandChild);

    if (denormalizeConfiguration != null) {
      RelatedTablesConfiguration targetTable = denormalizeConfiguration.getRelatedTable(childNode.getUuid());
      if (targetTable != null) {
        switchBtn.getButton().setValue(true, false);
        grandChild.add(expandLevel(childNode));
        card.setHideContentVisible(true);
        card.removeStyleName("card-disabled");
        container.add(selectTable);
      }
    }

    return panel;
  }

  private FlowPanel expandLevel(TableNode node) {
    FlowPanel relationShipList = new FlowPanel();
    for (Map.Entry<ViewerForeignKey, TableNode> entry : node.getChildren().entrySet()) {
      TableNode childNode = entry.getValue();
      childNode.setParentNode(node, entry.getKey());
      childNode.setupChildren();
      relationShipList.add(createChildTableCard(childNode));
    }
    return relationShipList;
  }

  private FlowPanel getInformationAboutRelationship(TableNode node) {
    FlowPanel information = new FlowPanel();
    ViewerForeignKey foreignKey = node.getForeignKey();
    ViewerTable referencedTable = node.getParentNode().getTable();
    ViewerTable sourceTable = node.getTable();

    for (ViewerReference reference : foreignKey.getReferences()) {
      boolean isVirtual = ViewerSourceType.VIRTUAL.equals(foreignKey.getSourceType());

      if (foreignKey.getReferencedTableUUID().equals(referencedTable.getUuid())) {
        ViewerColumn column = DataTransformationUtils.getColumnByIndex(sourceTable.getColumns(),
          reference.getSourceColumnIndex());
        information.add(buildReferenceInformation(
          messages.dataTransformationTextForIsRelatedTo(referencedTable.getId(), column.getDisplayName()), isVirtual));
      } else {
        ViewerColumn column = DataTransformationUtils.getColumnByIndex(sourceTable.getColumns(),
          reference.getReferencedColumnIndex());
        information.add(buildReferenceInformation(
          messages.dataTransformationTextForIsReferencedBy(referencedTable.getId(), column.getDisplayName()),
          isVirtual));
      }
    }

    return information;
  }

  private FlowPanel buildReferenceInformation(SafeHtml message, boolean isVirtual) {
    FlowPanel referenceInformation = new FlowPanel();
    referenceInformation.setStyleName("reference-panel");

    HTML referenceIcon = new HTML(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.REFERENCE)));
    referenceIcon.setStyleName("icon");
    referenceInformation.add(referenceIcon);
    referenceInformation.add(new HTML(message));

    if (isVirtual) {
      FlowPanel virtualLabel = new FlowPanel();
      Label label = new Label(messages.dataTransformationLabelForVirtualRelationship());
      label.addStyleName("label-info");
      virtualLabel.add(label);
      referenceInformation.add(virtualLabel);
    }

    return referenceInformation;
  }

  private void updateControllerPanel() {
    if (!isInformation) {
      btnSaveConfiguration.setEnabled(false);
      btnCancel.setEnabled(false);

      for (DenormalizeConfiguration config : denormalizeConfigurationList.values()) {
        if (config != null && ViewerJobStatus.NEW.equals(config.getState())) {
          btnSaveConfiguration.setEnabled(true);
          btnCancel.setEnabled(true);
          break;
        }
      }

      List<Button> sidebarButtons = new ArrayList<>();
      sidebarButtons.add(btnSaveConfiguration);
      sidebarButtons.add(btnCancel);
      sidebar.updateControllerPanel(sidebarButtons);
    } else {
      sidebar.updateControllerPanel(null);
    }
  }

  public void saveConfiguration(String databaseUUID,
    Set<Map.Entry<String, DenormalizeConfiguration>> denormalizeConfigurationSet) {
    Map<String, DenormalizeConfiguration> configsToSave = new HashMap<>();
    for (Map.Entry<String, DenormalizeConfiguration> entry : denormalizeConfigurationSet) {
      DenormalizeConfiguration config = entry.getValue();
      if (config != null && ViewerJobStatus.NEW.equals(config.getState())) {
        configsToSave.put(entry.getKey(), config);
      }
    }

    if (configsToSave.isEmpty())
      return;

    CollectionService.Util.call((Boolean result) -> {
      Toast.showInfo(messages.advancedConfigurationLabelForDataTransformation(), "Configurations saved successfully.");

      CollectionService.Util.call((ConfigurationContext context) -> {
        if (context != null && context.getProjectedDatabase() != null) {
          database = context.getProjectedDatabase();
          collectionStatus = context.getCollectionStatus();
          ObserverManager.getCollectionObserver().setCollectionStatus(collectionStatus);

          isInitialized = true;
          configurationStatusPanel.setDatabase(database);

          if (sidebar != null) {
            sidebar.reset(database, collectionStatus);
          }
          rebuildDynamicUI();
        }
      }).getConfigurationContext(databaseUUID, databaseUUID);

    }, errorMessage -> {
      Dialogs.showErrors("Error Saving Configurations", errorMessage, messages.basicActionClose());
    }).createDenormalizeConfigurationFiles(databaseUUID, databaseUUID, configsToSave);
  }
}
