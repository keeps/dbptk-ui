package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.Method;

import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.common.LoadingDiv;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.lists.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.utils.TableUtils;
import com.databasepreservation.common.client.common.utils.Tree;
import com.databasepreservation.common.client.common.visualization.browse.configuration.handler.DenormalizeConfigurationHandler;
import com.databasepreservation.common.client.common.visualization.browse.information.ErDiagram;
import com.databasepreservation.common.client.models.structure.*;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.widgets.Alert;
import com.databasepreservation.common.client.widgets.BootstrapCard;
import com.databasepreservation.common.client.widgets.SwitchBtn;
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
public class DataTransformation extends RightPanel {
  interface DataTransformationUiBinder extends UiBinder<Widget, DataTransformation> {
  }

  private static DataTransformationUiBinder binder = GWT.create(DataTransformationUiBinder.class);
  private static Map<String, DataTransformation> instances = new HashMap<>();

  @UiField
  public ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel content;

  @UiField
  ScrollPanel scrollPanel;

  @UiField
  FlowPanel rootTablePanel;

  @UiField
  LoadingDiv loading;

  private ViewerDatabase database;
  private ViewerMetadata metadata;
  private ViewerTable rootTable;
  private DenormalizeConfigurationHandler configuration;

  /**
   * Used for show resume and ERDiagram
   * 
   * @param database
   * @return
   */
  public static DataTransformation getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new DataTransformation(database, null));
  }

  /**
   * Used for Show Tables with relationship
   * 
   * @param database
   * @param tableUUID
   * @return
   */
  public static DataTransformation getInstance(ViewerDatabase database, String tableUUID) {
    return instances.computeIfAbsent(database.getUuid() + tableUUID, k -> new DataTransformation(database, tableUUID));
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forDataTransformation(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  /**
   * Use DataTransformation.getInstance to obtain instance
   */
  private DataTransformation(ViewerDatabase database, String tableUUID) {
    initWidget(binder.createAndBindUi(this));
    this.database = database;
    this.metadata = database.getMetadata();
    this.rootTable = metadata.getTable(tableUUID);
    if (tableUUID == null) {
      content.add(ErDiagram.getInstance(database, database.getMetadata().getSchemas().get(0)));
    } else {
      getDenormalizeConfigurationFile();
    }
  }

  /**
   * Check if exist a configuration file for this database, if exist use to
   */
  private void getDenormalizeConfigurationFile() {
    loading.setVisible(true);
    configuration = DenormalizeConfigurationHandler.getInstance(database, rootTable);
    configuration.getCollectionConfiguration(new DefaultMethodCallback<Boolean>() {
      @Override
      public void onSuccess(Method method, Boolean response) {
        loading.setVisible(false);
        init();
      }
    });
  }

  private void init() {
    Tree<String> rootNode = new Tree<>(rootTable.getId());
    FlowPanel relationshipPanel = createRelationshipList(rootTable, rootNode);
    relationshipPanel.setStyleName("data-transformation-panel");
    Alert alert = new Alert(Alert.MessageAlertType.INFO, messages.dataTransformationTextForAlertColumnsOrder(), true,
      FontAwesomeIconManager.DATABASE_INFORMATION);

    scrollPanel.setSize("100%", "100%");
    content.add(alert);
    content.add(createTargetTableCard(rootTable));
    content.add(relationshipPanel);
    content.add(createJsonBtn());
  }

  private Button createJsonBtn() {
    Button btn = new Button("Json", new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        configuration.build();
      }
    });
    btn.setStyleName("btn btn-link");
    return btn;
  }

  /**
   * Creates the root card of relationship tree
   * 
   * @param table
   * @return BootstrapCard
   */
  private BootstrapCard createTargetTableCard(ViewerTable table) {
    rootTablePanel = new FlowPanel();
    rootTablePanel.add(TransformationTable.getInstance(database, rootTable, configuration));

    BootstrapCard card = new BootstrapCard();
    card.setTitleIcon(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE));
    card.setTitle(table.getId());
    card.setDescription(table.getDescription());
    card.addExtraContent(rootTablePanel);
    return card;
  }

  /**
   * Creates the child cards of relationship tree, this method will be called
   * recursively
   * 
   * @param table
   * @param parentNode
   * @return a wrapper of BootstrapCard
   */
  private FlowPanel createRelationshipList(ViewerTable table, Tree<String> parentNode) {
    FlowPanel panel = new FlowPanel();

    for (Map.Entry<ViewerForeignKey, ViewerTable> entry : getListOfRelationalTables(table).entrySet()) {

      ViewerTable relatedTable = entry.getValue();
      ViewerForeignKey foreignKey = entry.getKey();
      Tree<String> childNode = new Tree<>(relatedTable.getId());

      if (parentNode.searchTop(relatedTable.getId()) != null) {
        continue;
      }

      childNode.setParent(parentNode);
      panel.add(createRelationshipWrapper(table, relatedTable, childNode));
    }

    return panel;
  }

  /**
   * Scans all schemas and checks if table is referenced or appears in other
   * tables
   * 
   * @param table
   * @return a List of relation Tables
   */
  private Map<ViewerForeignKey, ViewerTable> getListOfRelationalTables(ViewerTable table) {
    Map<ViewerForeignKey, ViewerTable> tableMap = new HashMap<>();

    // retrieve all tables referenced in foreign keys
    for (ViewerForeignKey fk : table.getForeignKeys()) {
      ViewerTable referencedTable = metadata.getTable(fk.getReferencedTableUUID());
      tableMap.put(fk, referencedTable);
    }

    // retrieve all tables where this table is referenced
    for (ViewerSchema viewerSchema : metadata.getSchemas()) {
      for (ViewerTable viewerTable : viewerSchema.getTables()) {
        for (ViewerForeignKey fk : viewerTable.getForeignKeys()) {
          if (fk.getReferencedTableUUID().equals(table.getUUID())) {
            tableMap.put(fk, viewerTable);
          }
        }
      }
    }

    return tableMap;
  }

  /**
   * Wrapper will contain the current child card of tree and your relationships
   * 
   * @param table
   * @param relatedTable
   * @param childNode
   * @return a Wrapper with a BootstrapCard and a data-transformation-child
   */
  private FlowPanel createRelationshipWrapper(ViewerTable table, ViewerTable relatedTable, Tree<String> childNode) {
    BootstrapCard card = createChildTableCard(table, relatedTable);
    card.setHideContentVisible(false);
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("data-transformation-wrapper");

    FlowPanel container = new FlowPanel();
    FlowPanel child = new FlowPanel();
    child.addStyleName("data-transformation-child");

    SwitchBtn switchBtn = new SwitchBtn("Enable", false);
    FlowPanel switchPanel = new FlowPanel();
    switchPanel.add(switchBtn);
    switchBtn.setClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        switchBtn.getButton().setValue(!switchBtn.getButton().getValue(), true); // workaround for ie11
        card.setHideContentVisible(switchBtn.getButton().getValue());

        container.clear();

        child.clear();
        if (switchBtn.getButton().getValue()) {
          child.add(createRelationshipList(relatedTable, childNode));

          String uuid = configuration.includeRelatedTable(relatedTable, table);
          card.setUuid(uuid);
          TransformationChildTables tableInstance = TransformationChildTables.getInstance(relatedTable, configuration,
            uuid);
          MultipleSelectionTablePanel selectTable = tableInstance.createTable();
          container.add(selectTable);
        } else {
          configuration.removeRelatedTableByUUID(card.getUuid());
        }
      }
    });

    // RelatedTablesConfiguration relatedTablesConfiguration = configuration
    // .getRelatedTableByRelationship(relatedTable.getUUID(), table.getUUID());
    // if (relatedTablesConfiguration != null) {
    // String uuid = relatedTablesConfiguration.getUuid();
    // switchBtn.getButton().setValue(true, true);
    // card.setHideContentVisible(true);
    // card.setUuid(uuid);
    // child.add(createRelationshipList(relatedTable, childNode));
    // container.add(TransformationChildTables.getInstance(relatedTable,
    // configuration, uuid).createTable());
    // }

    card.addHideContent(container, switchPanel);
    panel.add(card);
    panel.add(child);
    return panel;
  }

  /**
   * Creates the child card of relationship tree
   * 
   * @param table
   * @return BootstrapCard
   */
  private BootstrapCard createChildTableCard(ViewerTable table, ViewerTable relatedTable) {
    BootstrapCard card = new BootstrapCard();
    card.setTitleIcon(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE));
    card.setTitle(relatedTable.getId());
    card.setDescription(relatedTable.getDescription());
    card.addExtraContent(getInformationAboutRelationship(table, relatedTable));

    return card;
  }

  /**
   * Add information in card about your relationship
   *
   *
   * @param tableA
   * @param tableB
   * @return
   */
  private FlowPanel getInformationAboutRelationship(ViewerTable tableA, ViewerTable tableB) {
    FlowPanel information = new FlowPanel();

    Map<String, List<ViewerForeignKey>> relationship = TableUtils.getRelationship(tableA, tableB);

    // for (Map.Entry<String, List<ViewerForeignKey>> entry :
    // relationship.entrySet()) {
    // for (ViewerForeignKey foreignKey : entry.getValue()) {
    // for (ViewerReference reference : foreignKey.getReferences()) {
    // if (foreignKey.getReferencedTableUUID().equals(tableA.getUUID())) {
    // ViewerColumn column =
    // metadata.getTable(foreignKey.getReferencedTableUUID()).getColumns()
    // .get(reference.getReferencedColumnIndex());
    // information.add(buildReferenceInformation(
    // messages.dataTransformationTextForIsRelatedTo(tableA.getId(),
    // column.getDisplayName())));
    // } else {
    // ViewerColumn column = metadata.getTable(tableA.getUUID()).getColumns()
    // .get(reference.getSourceColumnIndex());
    // information.add(buildReferenceInformation(
    // messages.dataTransformationTextForIsReferencedBy(tableA.getId(),
    // column.getDisplayName())));
    // }
    // }
    //
    // }
    // }

    for (ViewerForeignKey foreignKey : TableUtils.checkForeignKey(tableA, tableB)) {
      for (ViewerReference reference : foreignKey.getReferences()) {
        ViewerColumn column = metadata.getTable(foreignKey.getReferencedTableUUID()).getColumns()
          .get(reference.getReferencedColumnIndex());
        information.add(buildReferenceInformation(
          messages.dataTransformationTextForIsRelatedTo(tableA.getId(), column.getDisplayName())));
      }
    }

    for (ViewerForeignKey foreignKey : TableUtils.checkForeignKey(tableB, tableA)) {
      for (ViewerReference reference : foreignKey.getReferences()) {
        ViewerColumn column = metadata.getTable(tableA.getUUID()).getColumns().get(reference.getSourceColumnIndex());
        information.add(buildReferenceInformation(
          messages.dataTransformationTextForIsReferencedBy(tableA.getId(), column.getDisplayName())));
      }
    }

    // if (foreignKey.getReferencedTableUUID().equals(tableA.getUUID())) {
    // for (ViewerReference reference : foreignKey.getReferences()) {
    // ViewerColumn column =
    // metadata.getTable(foreignKey.getReferencedTableUUID()).getColumns()
    // .get(reference.getReferencedColumnIndex());
    // information.add(buildReferenceInformation(
    // messages.dataTransformationTextForIsRelatedTo(tableA.getId(),
    // column.getDisplayName())));
    // }
    // } else {
    // for (ViewerReference reference : foreignKey.getReferences()) {
    // ViewerColumn column =
    // metadata.getTable(tableA.getUUID()).getColumns().get(reference.getSourceColumnIndex());
    // information.add(buildReferenceInformation(
    // messages.dataTransformationTextForIsReferencedBy(tableA.getId(),
    // column.getDisplayName())));
    // }
    // }

    return information;
  }

  /**
   * Create a reference information panel
   * 
   * @param message
   * @return FlowPanel
   */
  private FlowPanel buildReferenceInformation(String message) {
    FlowPanel referenceInformation = new FlowPanel();
    referenceInformation.setStyleName("reference-panel");

    // icon
    HTML referenceIcon = new HTML(
      SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.REFERENCE)));
    referenceIcon.setStyleName("icon");
    referenceInformation.add(referenceIcon);
    referenceInformation.add(new Label(message));

    return referenceInformation;
  }
}