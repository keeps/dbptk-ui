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
import com.databasepreservation.common.client.common.StatusAwareRightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.sidebar.DataTransformationSidebar;
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
public class DataTransformation extends StatusAwareRightPanel implements ICollectionStatusObserver {

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

    updateStatusPanel(this.database);

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

        updateStatusPanel(this.database);

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

    TableNode rootNode = new TableNode(database, table, collectionStatus);
    rootNode.setupPossibleTargets(denormalizeConfiguration.getRelatedTables());

    Widget rootCard = createRootTableCard(rootNode.getTable());
    rootTablePanel.add(rootCard);
    dynamicWidgets.add(rootCard);

    FlowPanel relationShipPanel = new FlowPanel();
    relationShipPanel.setStyleName("data-transformation-panel");
    relationShipPanel.add(expandLevel(rootNode, denormalizeConfiguration.getRelatedTables()));
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

  private FlowPanel createTargetTablePanel(TableNode targetNode,
    List<RelatedTablesConfiguration> alreadyIncludedTargetRelatedTables) {
    FlowPanel targetPanel = new FlowPanel();
    targetPanel.addStyleName("data-transformation-wrapper");

    FlowPanel subTargetPanels = new FlowPanel();
    subTargetPanels.addStyleName("data-transformation-child");

    ViewerTable targetTable = targetNode.getTable();
    BootstrapCard targetCard = new BootstrapCard();

    targetCard.setTitleIcon(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE));

    String targetCardTitle = targetTable.getName();
    if (collectionStatus.getTableStatusByTableId(targetTable.getId()) != null) {
      String customName = collectionStatus.getTableStatusByTableId(targetTable.getId()).getCustomName();
      if (customName != null && !customName.trim().isEmpty())
        targetCardTitle = customName;
    }
    targetCard.setTitle(targetCardTitle);
    targetCard.setDescription(targetTable.getDescription());
    targetCard.addStyleName("card-disabled");
    targetCard.addExtraContent(getInformationAboutRelationship(targetNode));
    targetCard.getElement().setId(targetNode.getDenormalizationUUID());

    if (targetNode.getIsVirtual())
      targetCard.addStyleName("card-virtual");

    FlowPanel targetSelectPanelContainer = new FlowPanel();
    TransformationChildTables targetTransformationChildTables = TransformationChildTables.createInstance(targetNode,
      denormalizeConfiguration, rootTable, buttons);
    MultipleSelectionTablePanel<ViewerColumn> selectTable = targetTransformationChildTables.createTable();

    SwitchBtn targetEnabledSwitchButton = new SwitchBtn("Enable", false);
    targetEnabledSwitchButton.setClickHandler(event -> {
      targetEnabledSwitchButton.getButton().setValue(!targetEnabledSwitchButton.getButton().getValue(), true);
      if (targetEnabledSwitchButton.getButton().getValue()) {
        targetCard.removeStyleName("card-disabled");
        subTargetPanels.add(expandLevel(targetNode, alreadyIncludedTargetRelatedTables));
        DataTransformationUtils.includeRelatedTable(targetNode, denormalizeConfiguration);
        targetSelectPanelContainer.add(selectTable);
      } else {
        DataTransformationUtils.removeRelatedTable(targetNode, denormalizeConfiguration);
        targetCard.addStyleName("card-disabled");
        subTargetPanels.clear();
        targetSelectPanelContainer.clear();
        selectTable.getSelectionModel().clear();
        rootTable.redrawTable(denormalizeConfiguration);
        buttons.forEach(button -> button.setEnabled(true));
      }
    });

    FlowPanel targetEnabledSwitchPanel = new FlowPanel();
    targetEnabledSwitchPanel.add(targetEnabledSwitchButton);
    targetCard.addHideContent(targetSelectPanelContainer, targetEnabledSwitchPanel);

    targetPanel.add(targetCard);
    targetPanel.add(subTargetPanels);

    if (denormalizeConfiguration != null) {
      RelatedTablesConfiguration targetTableRelatedTableConfig = denormalizeConfiguration
        .getRelatedTable(targetNode.getDenormalizationUUID());
      if (targetTableRelatedTableConfig != null) {
        targetEnabledSwitchButton.getButton().setValue(true, false);
        subTargetPanels.add(expandLevel(targetNode, alreadyIncludedTargetRelatedTables));
        targetCard.setHideContentVisible(true);
        targetCard.removeStyleName("card-disabled");
        targetSelectPanelContainer.add(selectTable);
      }
    }

    return targetPanel;
  }

  private FlowPanel expandLevel(TableNode sourceNode,
    List<RelatedTablesConfiguration> sourceNodeAlreadyIncludedRelatedTables) {
    FlowPanel relationShipList = new FlowPanel();
    for (TableNode targetNode : sourceNode.getPossibleTargetTables()) {
      RelatedTablesConfiguration sourceTargetRelatedTableConfiguration = DataTransformationUtils
        .getRelatedTableConfiguration(sourceNodeAlreadyIncludedRelatedTables, targetNode.getTable().getUuid(),
          targetNode.getSourceDenormalizationDirection());
      List<RelatedTablesConfiguration> targetRelatedTablesConfigurations;
      if (sourceTargetRelatedTableConfiguration == null) {
        targetRelatedTablesConfigurations = List.of();
      } else {
        targetRelatedTablesConfigurations = sourceTargetRelatedTableConfiguration.getRelatedTables();
      }
      targetNode.setupPossibleTargets(targetRelatedTablesConfigurations);
      relationShipList.add(createTargetTablePanel(targetNode, targetRelatedTablesConfigurations));
    }
    return relationShipList;
  }

  private FlowPanel getInformationAboutRelationship(TableNode targetTableNode) {
    FlowPanel information = new FlowPanel();
    ViewerForeignKey foreignKey = targetTableNode.getForeignKey();
    ViewerTable sourceTable = targetTableNode.getSourceNode().getTable();
    ViewerTable targetTable = targetTableNode.getTable();

    for (ViewerReference reference : foreignKey.getReferences()) {
      boolean isVirtual = ViewerSourceType.VIRTUAL.equals(foreignKey.getSourceType());

      if (targetTableNode.getSourceDenormalizationDirection()
        .equals(ViewerConstants.DENORMALIZATION_DIRECTION_TARGET_TO_SOURCE)) {
        ViewerColumn column = DataTransformationUtils.getColumnByIndex(targetTable.getColumns(),
          reference.getSourceColumnIndex());
        information.add(buildReferenceInformation(
          messages.dataTransformationTextForTargetToSource(sourceTable.getId(), column.getDisplayName()), isVirtual));
      } else {
        ViewerColumn column = DataTransformationUtils.getColumnByIndex(targetTable.getColumns(),
          reference.getReferencedColumnIndex());
        information.add(buildReferenceInformation(
          messages.dataTransformationTextForSourceToTarget(sourceTable.getId(), column.getDisplayName()),
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
          updateStatusPanel(database);

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
