package com.databasepreservation.common.client.common.visualization.browse.configuration.columns;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.cells.EditableCell;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.sidebar.Sidebar;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.configuration.observer.CollectionObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.helpers.TableStatusHelper;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.ConfigurationService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ColumnsManagementPanel extends RightPanel {
  private ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  SimplePanel mainHeader;

  @UiField
  FlowPanel content;

  interface TableManagementPanelUiBinder extends UiBinder<Widget, ColumnsManagementPanel> {
  }

  private static TableManagementPanelUiBinder binder = GWT.create(TableManagementPanelUiBinder.class);

  private static Map<String, ColumnsManagementPanel> instances = new HashMap<>();
  private ViewerDatabase database;
  private CollectionStatus collectionStatus;
  private Map<String, MultipleSelectionTablePanel<ViewerTable>> tables = new HashMap<>();
  private Map<String, Boolean> initialLoading = new HashMap<>();
  private Map<String, TableStatusHelper> editableValues = new HashMap<>();

  public static ColumnsManagementPanel getInstance(ViewerDatabase database, CollectionStatus status) {
    return instances.computeIfAbsent(database.getUuid(),
      k -> new ColumnsManagementPanel(database, status, null));
  }

  public static ColumnsManagementPanel getInstance(CollectionStatus status, ViewerDatabase database, String tableUUID) {
    return instances.computeIfAbsent(database.getUuid() + tableUUID,
      k -> new ColumnsManagementPanel(database, status, tableUUID));
  }

  private ColumnsManagementPanel(ViewerDatabase database, CollectionStatus collectionStatus, String tableUUID) {
    initWidget(binder.createAndBindUi(this));
    this.database = database;
    this.collectionStatus = collectionStatus;
    if (tableUUID != null) {
      init();
    }
  }

  private void init() {
    configureHeader();
    // for (ViewerSchema schema : database.getMetadata().getSchemas()) {
    // MultipleSelectionTablePanel<ViewerTable> schemaTable =
    // createCellTableForViewerTable();
    // schemaTable.setHeight("100%");
    // populateTable(schemaTable,
    // database.getMetadata().getSchema(schema.getUuid()));
    // tables.put(schema.getUuid(), schemaTable);
    // content.add(schemaTable);
    // }

    configureButtonsPanel();
  }

  private void configureButtonsPanel() {
    Button btnCancel = new Button();
    btnCancel.setText(messages.basicActionCancel());
    btnCancel.addStyleName("btn btn-danger btn-times-circle");
    Button btnUpdate = new Button();
    btnUpdate.setText(messages.basicActionSave());
    btnUpdate.addStyleName("btn btn-primary btn-save");

    btnCancel.addClickHandler(clickEvent -> HistoryManager.gotoAdvancedConfiguration(database.getUuid()));

    btnUpdate.addClickHandler(clickEvent -> {
      for (MultipleSelectionTablePanel<ViewerTable> value : tables.values()) {
        for (ViewerTable table : database.getMetadata().getTables().values()) {
          collectionStatus.updateTableHidingCondition(table.getUuid(), !value.getSelectionModel().isSelected(table));
          if (editableValues.get(table.getUuid()) != null) {
            collectionStatus.updateTableCustomDescription(table.getUuid(),
              editableValues.get(table.getUuid()).getDescription());
            collectionStatus.updateTableCustomName(table.getUuid(), editableValues.get(table.getUuid()).getLabel());
          }
        }
      }

      ConfigurationService.Util.call((Boolean result) -> {
        final CollectionObserver collectionObserver = ObserverManager.getCollectionObserver();
        collectionObserver.setCollectionStatus(collectionStatus);
        Toast.showInfo(messages.columnManagementPageTitle(), messages.columnManagementPageToastDescription());
      }).updateCollectionStatus(database.getUuid(), database.getUuid(), collectionStatus);
    });

    content.add(CommonClientUtils.wrapOnDiv("navigation-panel-buttons",
      CommonClientUtils.wrapOnDiv("btn-item", btnCancel), CommonClientUtils.wrapOnDiv("btn-item", btnUpdate)));
  }

  private void configureHeader() {
    mainHeader.setWidget(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE),
      messages.columnManagementPageTitle(), "h1"));

    MetadataField instance = MetadataField.createInstance(messages.columnManagementPageTableTextForDescription());
    instance.setCSS("table-row-description", "font-size-description");

    content.add(instance);
  }

  private MultipleSelectionTablePanel<ViewerTable> createCellTableForViewerTable() {
    return new MultipleSelectionTablePanel<>();
  }

  private void populateTable(MultipleSelectionTablePanel<ViewerTable> selectionTablePanel,
    final ViewerSchema viewerSchema) {
    selectionTablePanel.createTable(new FlowPanel(), Arrays.asList(1, 2), viewerSchema.getTables().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForShow(), 4,
        new Column<ViewerTable, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ViewerTable viewerTable) {
            if (initialLoading.get(viewerTable.getUuid()) == null) {
              initialLoading.put(viewerTable.getUuid(), false);
              selectionTablePanel.getSelectionModel().setSelected(viewerTable,
                collectionStatus.showTable(viewerTable.getUuid()));
            }

            return selectionTablePanel.getSelectionModel().isSelected(viewerTable);
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForOriginalTableName(),
        10, new TextColumn<ViewerTable>() {
          @Override
          public String getValue(ViewerTable table) {
            return table.getName();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForLabel(), 15,
        getLabelColumn()),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForDescription(), 0,
        getDescriptionColumn()));
  }

  private Column<ViewerTable, String> getLabelColumn() {
    Column<ViewerTable, String> label = new Column<ViewerTable, String>(new EditableCell() {}) {
      @Override
      public String getValue(ViewerTable table) {
        return collectionStatus.getTableStatus(table.getUuid()).getCustomName();
      }
    };

    label.setFieldUpdater((index, table, value) -> {
      if (editableValues.get(table.getUuid()) != null) {
        final TableStatusHelper tableStatusHelper = editableValues.get(table.getUuid());
        tableStatusHelper.setLabel(value);
        editableValues.put(table.getUuid(), tableStatusHelper);
      } else {
        TableStatusHelper helper = new TableStatusHelper(value,
          collectionStatus.getTableStatus(table.getUuid()).getCustomDescription());
        editableValues.put(table.getUuid(), helper);
      }
    });
    return label;
  }

  private Column<ViewerTable, String> getDescriptionColumn() {
    Column<ViewerTable, String> description = new Column<ViewerTable, String>(new EditableCell() {}) {
      @Override
      public String getValue(ViewerTable table) {
        return collectionStatus.getTableStatus(table.getUuid()).getCustomDescription();
      }
    };

    description.setFieldUpdater((index, table, value) -> {
      if (editableValues.get(table.getUuid()) != null) {
        final TableStatusHelper tableStatusHelper = editableValues.get(table.getUuid());
        tableStatusHelper.setDescription(value);
        editableValues.put(table.getUuid(), tableStatusHelper);
      } else {
        TableStatusHelper helper = new TableStatusHelper(
          collectionStatus.getTableStatus(table.getUuid()).getCustomName(), value);
        editableValues.put(table.getUuid(), helper);
      }
    });
    return description;
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forColumnsManagement(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }
}