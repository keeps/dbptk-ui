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

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.LoadingDiv;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.sidebar.DataTransformationSidebar;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
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
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.BootstrapCard;
import com.databasepreservation.common.client.widgets.SwitchBtn;
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
  @Override
  public void updateCollection(CollectionStatus collectionStatus) {
    instances.clear();
    this.collectionStatus = collectionStatus;
  }

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
  LoadingDiv loading;

  private ViewerDatabase database;
  private ViewerTable table;
  private String tableId;
  private TransformationTable rootTable;
  private DataTransformationSidebar sidebar;
  private CollectionStatus collectionStatus;
  private DenormalizeConfiguration denormalizeConfiguration;
  private Button btnRunConfiguration = new Button();
  private Button btnRunAllConfiguration = new Button();
  private Button btnGotoTable = new Button();
  private Button btnCancel = new Button();
  private List<Button> buttons = new ArrayList<>();
  private List<Button> buttonsToSidebar = new ArrayList<>();
  private Boolean isInformation;

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems;
    breadcrumbItems = BreadcrumbManager.forDataTransformation(database.getUuid(), database.getMetadata().getName(),
      messages.breadcrumbTextForDataTransformation());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  public static DataTransformation getInstance(CollectionStatus collectionStatus, ViewerDatabase database,
    DataTransformationSidebar sidebar) {
    return instances.computeIfAbsent(database.getUuid(),
      k -> new DataTransformation(collectionStatus, database, null, sidebar));
  }

  public static DataTransformation getInstance(CollectionStatus collectionStatus, ViewerDatabase database,
    String tableId, DataTransformationSidebar sidebar) {
    return instances.computeIfAbsent(database.getUuid() + tableId,
      k -> new DataTransformation(collectionStatus, database, tableId, sidebar));
  }

  private DataTransformation(CollectionStatus collectionStatus, ViewerDatabase database, String tableId,
    DataTransformationSidebar sidebar) {
    initWidget(binder.createAndBindUi(this));
    ObserverManager.getCollectionObserver().addObserver(this);
    this.database = database;
    this.sidebar = sidebar;
    this.collectionStatus = collectionStatus;
    this.tableId = tableId;
    if (this.tableId == null) {
      isInformation = true;
      content.add(informationPanel());
      toolBar.setVisible(false);
    } else {
      isInformation = false;
      getDenormalizeConfigurationFile(this.tableId);
    }
  }

  private void createToolBar() {
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
    panel.add(btnRunConfiguration);
    panel.add(btnGotoTable);

    toolBar.add(tablePanel);
    toolBar.add(panel);
  }

  private ErDiagram informationPanel() {
    return ErDiagram.getInstance(database, database.getMetadata().getSchemas().get(0),
      HistoryManager.getCurrentHistoryPath().get(0), collectionStatus);
  }

  /**
   * Check if exist a configuration file for this database, if exist use to
   */
  private void getDenormalizeConfigurationFile(String tableId) {
    loading.setVisible(true);
    this.table = database.getMetadata().getTableById(tableId);
    if (collectionStatus.getDenormalizations()
      .contains(ViewerConstants.DENORMALIZATION_STATUS_PREFIX + table.getUuid())) {
      CollectionService.Util.call((DenormalizeConfiguration response) -> {
        denormalizeConfiguration = response;
        init();
      }).getDenormalizeConfigurationFile(database.getUuid(), database.getUuid(), table.getUuid());
    } else {
      denormalizeConfiguration = new DenormalizeConfiguration(database.getUuid(), table);
      init();
    }
  }

  private void init() {
    denormalizeConfigurationList.put(table.getUuid(), denormalizeConfiguration);
    loading.setVisible(false);

    Alert alert = new Alert(Alert.MessageAlertType.INFO, messages.dataTransformationTextForAlertColumnsOrder(), true,
      FontAwesomeIconManager.DATABASE_INFORMATION);
    message.setWidget(alert);

    // root table
    TableNode parentNode = new TableNode(database, table);
    parentNode.setUuid(table.getUuid());
    parentNode.setupChildren();
    ViewerTable table = parentNode.getTable();
    content.add(createRootTableCard(table));

    // relationship list
    FlowPanel relationShipPanel = new FlowPanel();
    relationShipPanel.setStyleName("data-transformation-panel");
    relationShipPanel.add(expandLevel(parentNode));
    content.add(relationShipPanel);

    createButtons();
    createToolBar();
  }

  private void createButtons() {
    btnCancel.setEnabled(false);
    btnCancel.setText(messages.basicActionCancel());
    btnCancel.addStyleName("btn btn-times-circle btn-danger");
    btnCancel.addClickHandler(clickEvent -> {
      instances.entrySet().removeIf(e -> e.getKey().startsWith(database.getUuid()));
      sidebar.reset(database, collectionStatus);
      HistoryManager.gotoAdvancedConfiguration(database.getUuid());
    });

    btnGotoTable = new Button();
    btnGotoTable.setText(messages.dataTransformationBtnBrowseTable());
    btnGotoTable.setStyleName("btn btn-table");
    btnGotoTable.addClickHandler(event -> HistoryManager.gotoTable(database.getUuid(), tableId));

    btnRunConfiguration.setEnabled(false);
    btnRunConfiguration.setText(
      messages.dataTransformationBtnRunTable());
    btnRunConfiguration.setStyleName("btn btn-play");
    btnRunConfiguration.addClickHandler(clickEvent -> {
        DataTransformationUtils.saveConfiguration(database.getUuid(), denormalizeConfiguration, collectionStatus);
        HistoryManager.gotoJobs();
    });

    btnRunAllConfiguration.setText(messages.dataTransformationBtnRunAll());
    btnRunAllConfiguration.setStyleName("btn btn-play");
    btnRunAllConfiguration.addClickHandler(clickEvent -> {
      HistoryManager.gotoJobs();
      for (Map.Entry<String, DenormalizeConfiguration> entry : denormalizeConfigurationList.entrySet()) {
        DataTransformationUtils.saveConfiguration(database.getUuid(), entry.getValue(), collectionStatus);
      }
    });

    buttons.add(btnCancel);
    buttons.add(btnRunConfiguration);
    buttons.add(btnRunAllConfiguration);

    updateControllerPanel();
  }

  /**
   * Creates the root card of relationship tree
   *
   * @return BootstrapCard
   */
  private BootstrapCard createRootTableCard(ViewerTable table) {
    BootstrapCard card = new BootstrapCard();
    // card.setTitleIcon(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE));
    // card.setTitle(table.getId());
    card.setDescription(table.getDescription());
    rootTable = TransformationTable.getInstance(database, table, denormalizeConfiguration);
    FlowPanel rootTablePanel = new FlowPanel();
    rootTablePanel.add(rootTable);
    card.addExtraContent(rootTablePanel);
    return card;
  }

  /**
   * Creates the child card of relationship tree
   *
   * @return BootstrapCard
   */
  private FlowPanel createChildTableCard(TableNode childNode) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("data-transformation-wrapper");

    FlowPanel grandChild = new FlowPanel();
    grandChild.addStyleName("data-transformation-child");

    ViewerTable childTable = childNode.getTable();
    BootstrapCard card = new BootstrapCard();

    card.setTitleIcon(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE));
    card.setTitle(childTable.getName());
    card.setDescription(childTable.getDescription());
    card.addStyleName("card-disabled");
    card.addExtraContent(getInformationAboutRelashionship(childNode));
    card.getElement().setId(childNode.getUuid());

    FlowPanel container = new FlowPanel();
    TransformationChildTables tableInstance = TransformationChildTables.createInstance(childNode, denormalizeConfiguration,
      rootTable, buttons);
    MultipleSelectionTablePanel<ViewerColumn> selectTable = tableInstance.createTable();

    SwitchBtn switchBtn = new SwitchBtn("Enable", false);

    switchBtn.setClickHandler(event -> {
      switchBtn.getButton().setValue(!switchBtn.getButton().getValue(), true); // workaround for ie11
      if (switchBtn.getButton().getValue()) {
        card.removeStyleName("card-disabled");
        grandChild.add(expandLevel(childNode));
        DataTransformationUtils.includeRelatedTable(childNode, denormalizeConfiguration);
        container.add(selectTable);
      } else {
        DataTransformationUtils.removeRelatedTable(childNode, denormalizeConfiguration);
        card.addStyleName("card-disabled");
        grandChild.clear();
        container.clear();
        selectTable.getSelectionModel().clear();
        rootTable.redrawTable(denormalizeConfiguration);
        for (Button button : buttons) {
          button.setEnabled(true);
        }
      }
    });

    FlowPanel switchPanel = new FlowPanel();
    switchPanel.add(switchBtn);

    card.addHideContent(container, switchPanel);

    panel.add(card);
    panel.add(grandChild);

    if (denormalizeConfiguration != null) {
      setup(childNode, switchBtn, card, grandChild, container, selectTable);
    }

    return panel;
  }

  private void setup(TableNode childNode, SwitchBtn switchBtn, BootstrapCard card, FlowPanel grandChild,
    FlowPanel container, MultipleSelectionTablePanel<ViewerColumn> selectTable) {
    RelatedTablesConfiguration targetTable = denormalizeConfiguration.getRelatedTable(childNode.getUuid());
    if (targetTable != null) {
      switchBtn.getButton().setValue(true, true);
      grandChild.add(expandLevel(childNode));
      card.setHideContentVisible(true);
      card.removeStyleName("card-disabled");
      container.add(selectTable);
    }
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

  private FlowPanel getInformationAboutRelashionship(TableNode node) {
    FlowPanel information = new FlowPanel();
    ViewerForeignKey foreignKey = node.getForeignKey();
    ViewerTable referencedTable = node.getParentNode().getTable();
    ViewerTable sourceTable = node.getTable();

    for (ViewerReference reference : foreignKey.getReferences()) {
      if (foreignKey.getReferencedTableUUID().equals(referencedTable.getUuid())) {
        ViewerColumn column = sourceTable.getColumns().get(reference.getSourceColumnIndex());
        information.add(buildReferenceInformation(
          messages.dataTransformationTextForIsRelatedTo(referencedTable.getId(), column.getDisplayName())));
      } else {
        ViewerColumn column = referencedTable.getColumns().get(reference.getSourceColumnIndex());
        information.add(buildReferenceInformation(
          messages.dataTransformationTextForIsReferencedBy(referencedTable.getId(), column.getDisplayName())));
      }
    }

    return information;
  }

  /**
   * Create a reference information panel
   *
   */
  private FlowPanel buildReferenceInformation(SafeHtml message) {
    FlowPanel referenceInformation = new FlowPanel();
    referenceInformation.setStyleName("reference-panel");

    // icon
    HTML referenceIcon = new HTML(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.REFERENCE)));
    referenceIcon.setStyleName("icon");
    referenceInformation.add(referenceIcon);
    referenceInformation.add(new HTML(message));

    return referenceInformation;
  }

  private void updateControllerPanel() {
    if (!isInformation) {
      btnRunAllConfiguration.setEnabled(false);
      btnCancel.setEnabled(false);
      // btnClearConfiguration.setEnabled(false);
      if (!denormalizeConfigurationList.isEmpty()) {
        for (DenormalizeConfiguration value : denormalizeConfigurationList.values()) {
          if (value.getState() != null && value.getState().equals(ViewerJobStatus.NEW)) {
            btnRunAllConfiguration.setEnabled(true);
            btnCancel.setEnabled(true);
            // btnClearConfiguration.setEnabled(true);
            break;
          }
        }
      }
      buttonsToSidebar.clear();
      buttonsToSidebar.add(btnRunAllConfiguration);
      buttonsToSidebar.add(btnCancel);
      sidebar.updateControllerPanel(buttonsToSidebar);
    } else {
      sidebar.updateControllerPanel(null);
    }
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    if (database != null) {
      updateControllerPanel();
    }
  }
}
