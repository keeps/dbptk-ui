package com.databasepreservation.common.client.common.visualization.browse.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

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

  private ViewerDatabase database;
  private ViewerMetadata metadata;
  private ViewerTable targetTable;
  private FlowPanel targetTablePanel = new FlowPanel();
  private Map<String, ViewerColumn> columnsToIncludeMap = new HashMap<>();

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

  private DataTransformation(ViewerDatabase database, String tableUUID) {
    initWidget(binder.createAndBindUi(this));
    this.database = database;
    this.metadata = database.getMetadata();
    if (tableUUID == null) {
      content.add(ErDiagram.getInstance(database, database.getMetadata().getSchemas().get(0)));
    } else {
      targetTable = database.getMetadata().getTable(tableUUID);
      content.add(new Alert(Alert.MessageAlertType.INFO, messages.dataTransformationTextForAlertColumnsOrder(), true,
        FontAwesomeIconManager.DATABASE_INFORMATION));
      content.add(createTargetTableCard(targetTable));
      FlowPanel relationshipPanel = createRelationshipList(targetTable);
      relationshipPanel.setStyleName("data-transformation-panel");
      content.add(relationshipPanel);
    }
  }

  /**
   * Creates the root card of relationship tree
   * 
   * @param table
   * @return BootstrapCard
   */
  private BootstrapCard createTargetTableCard(ViewerTable table) {
    targetTablePanel.add(new DataTransformationTables(targetTable).createTargetTable());

    BootstrapCard card = new BootstrapCard();
    card.setTitleIcon(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE));
    card.setTitle(table.getId());
    card.setDescription(table.getDescription());
    card.addExtraContent(targetTablePanel);
    return card;
  }

  /**
   * Creates the child cards of relationship tree, this method will be called
   * recursively
   * 
   * @param table
   * @return a wrapper of BootstrapCard
   */
  private FlowPanel createRelationshipList(ViewerTable table) {
    FlowPanel panel = new FlowPanel();

    for (ViewerTable relatedTable : getListOfRelationalTables(table)) {
      panel.add(createRelationshipWrapper(table, relatedTable));
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
  private List<ViewerTable> getListOfRelationalTables(ViewerTable table) {
    List<ViewerTable> tableList = new ArrayList<>();

    // retrieve all tables referenced in foreign keys
    for (ViewerForeignKey fk : table.getForeignKeys()) {
      ViewerTable referencedTable = metadata.getTable(fk.getReferencedTableUUID());
      tableList.add(referencedTable);
    }

    // retrieve all tables where this table is referenced
    for (ViewerSchema viewerSchema : metadata.getSchemas()) {
      for (ViewerTable viewerTable : viewerSchema.getTables()) {
        for (ViewerForeignKey fk : viewerTable.getForeignKeys()) {
          if (fk.getReferencedTableUUID().equals(table.getUUID())) {
            tableList.add(viewerTable);
          }
        }
      }
    }

    return tableList;
  }

  /**
   * Wrapper will contain the current child card of tree and your relationships
   * 
   * @param table
   * @param relatedTable
   * @return a Wrapper with a BootstrapCard and a data-transformation-child
   */
  private FlowPanel createRelationshipWrapper(ViewerTable table, ViewerTable relatedTable) {
    BootstrapCard card = createChildTableCard(table, relatedTable);
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
        container.add(new DataTransformationTables(relatedTable).createRelatedTable(targetTable, targetTablePanel,
          columnsToIncludeMap));

        child.clear();
        if (switchBtn.getButton().getValue()) {
          child.add(createRelationshipList(relatedTable));
        }
      }
    });

    card.addHideContent(container, switchPanel);
    card.setHideContentVisible(false);
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
   * @param table
   * @param relatedTable
   * @return
   */
  private FlowPanel getInformationAboutRelationship(ViewerTable table, ViewerTable relatedTable) {
    FlowPanel information = new FlowPanel();

    List<ViewerColumn> isReferenceOf = checkReference(table, relatedTable);
    List<ViewerColumn> isAppearsOn = checkReference(relatedTable, table);

    if (!isReferenceOf.isEmpty()) {
      for (ViewerColumn column : isReferenceOf) {
        information.add(buildReferenceInformation(
          messages.dataTransformationTextForIsReferencedBy(table.getId(), column.getDisplayName())));
      }
    } else if (!isAppearsOn.isEmpty()) {
      for (ViewerColumn column : isAppearsOn) {
        information.add(buildReferenceInformation(
          messages.dataTransformationTextForIsRelatedTo(table.getId(), column.getDisplayName())));
      }
    }

    return information;
  }

  /**
   * Check if there is relationship between two tables
   * @param target
   * @param source
   * @return a List of referenced columns
   */
  private List<ViewerColumn> checkReference(ViewerTable target, ViewerTable source) {
    List<ViewerColumn> referencesList = new ArrayList<>();
    for (ViewerForeignKey fk : target.getForeignKeys()) {
      if (fk.getReferencedTableUUID().equals(source.getUUID())) {
        for (ViewerReference reference : fk.getReferences()) {
          referencesList.add(target.getColumns().get(reference.getSourceColumnIndex()));
        }
      }
    }
    return referencesList;
  }

  /**
   * Create a reference information panel
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