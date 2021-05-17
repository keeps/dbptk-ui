/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.table;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.CommonDialogs;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.cells.RequiredEditableCell;
import com.databasepreservation.common.client.common.lists.cells.TextAreaInputCell;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.configuration.observer.CollectionObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.helpers.StatusHelper;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.ConfigurationCellTableResources;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableManagementPanel extends ContentPanel {
  private ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel header;

  @UiField
  FlowPanel content;

  interface TableManagementPanelUiBinder extends UiBinder<Widget, TableManagementPanel> {
  }

  private static TableManagementPanelUiBinder binder = GWT.create(TableManagementPanelUiBinder.class);

  private static Map<String, TableManagementPanel> instances = new HashMap<>();
  private ViewerDatabase database;
  private CollectionStatus collectionStatus;
  private Button btnSave = new Button();
  private boolean changes = false;
  private Map<String, MultipleSelectionTablePanel<ViewerTable>> tables = new HashMap<>();
  private Map<String, Boolean> initialLoading = new HashMap<>();

  private Map<String, Map<String, StatusHelper>> tableManagementData = new HashMap<>();

  public static TableManagementPanel getInstance(ViewerDatabase database, CollectionStatus status) {
    return instances.computeIfAbsent(database.getUuid(), k -> new TableManagementPanel(database, status));
  }

  private TableManagementPanel(ViewerDatabase database, CollectionStatus collectionStatus) {
    initWidget(binder.createAndBindUi(this));
    this.database = database;
    this.collectionStatus = collectionStatus;

    init();
  }

  private void init() {
    configureHeader();

    for (ViewerSchema schema : database.getMetadata().getSchemas()) {
      MultipleSelectionTablePanel<ViewerTable> schemaTable = createCellTableForViewerTable();
      schemaTable.setHeight("100%");
      populateTable(schemaTable, database.getMetadata().getSchema(schema.getUuid()));
      tables.put(schema.getUuid(), schemaTable);
      FlowPanel widgets = CommonClientUtils.wrapOnDiv("table-management-schema-divider",
        CommonClientUtils.getHeader(SafeHtmlUtils.fromSafeConstant(schema.getName()), "table-management-schema-title"),
        schemaTable);
      content.add(widgets);
    }

    configureButtonsPanel();
  }

  private void configureButtonsPanel() {
    Button btnCancel = new Button();
    btnCancel.setText(messages.basicActionCancel());
    btnCancel.addStyleName("btn btn-danger btn-times-circle");
    btnSave.setText(messages.basicActionSave());
    btnSave.addStyleName("btn btn-primary btn-save");

    btnCancel.addClickHandler(e -> handleCancelEvent(changes));

    btnSave.addClickHandler(clickEvent -> {
      if (selectionValidation()) {
        if (inputValidation()) {
          if (uniquenessValidation()) {
            for (MultipleSelectionTablePanel<ViewerTable> value : tables.values()) {
              for (ViewerTable table : database.getMetadata().getTables().values()) {
                collectionStatus.updateTableShowCondition(table.getUuid(), value.getSelectionModel().isSelected(table));
                if (!checkTableManagementDataIsEmpty(table)) {
                  collectionStatus.updateTableCustomDescription(table.getUuid(),
                    getTableManagementData(table).getDescription());
                  collectionStatus.updateTableCustomName(table.getUuid(), getTableManagementData(table).getLabel());
                }
              }
            }

            CollectionService.Util.call((Boolean result) -> {
              final CollectionObserver collectionObserver = ObserverManager.getCollectionObserver();
              collectionObserver.setCollectionStatus(collectionStatus);
              Toast.showInfo(messages.tableManagementPageTitle(), messages.tableManagementPageToastDescription());
              changes = false;
            }).updateCollectionConfiguration(database.getUuid(), database.getUuid(), collectionStatus);
          } else {
            Dialogs.showErrors(messages.tableManagementPageTitle(), messages.tableManagementPageDialogUniqueError(),
              messages.basicActionClose());
          }
        } else {
          Dialogs.showErrors(messages.tableManagementPageTitle(), messages.tableManagementPageDialogInputError(),
            messages.basicActionClose());
        }
      } else {
        Dialogs.showErrors(messages.tableManagementPageTitle(), messages.tableManagementPageDialogSelectionError(),
          messages.basicActionClose());
      }
    });

    content.add(CommonClientUtils.wrapOnDiv("navigation-panel-buttons",
      CommonClientUtils.wrapOnDiv("btn-item", btnSave), CommonClientUtils.wrapOnDiv("btn-item", btnCancel)));
  }

  private void configureHeader() {
    header.add(CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE),
      messages.tableManagementPageTitle(), "h1"));

    MetadataField instance = MetadataField.createInstance(messages.tableManagementPageTableTextForDescription());
    instance.setCSS("table-row-description", "font-size-description");
    content.add(instance);
  }

  private void handleCancelEvent(boolean changes) {
    if (changes) {
      CommonDialogs.showConfirmDialog(messages.columnManagementPageTitle(),
        SafeHtmlUtils.fromSafeConstant(messages.columnManagementPageCancelEventDialog()), messages.basicActionCancel(),
        messages.basicActionDiscard(), CommonDialogs.Level.DANGER, "400px", new DefaultAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean aBoolean) {
            if (aBoolean) {
              instances.entrySet().removeIf(e -> e.getKey().startsWith(database.getUuid()));
              HistoryManager.gotoAdvancedConfiguration(database.getUuid());
            }
          }
        });
    } else {
      HistoryManager.gotoAdvancedConfiguration(database.getUuid());
    }
  }

  private MultipleSelectionTablePanel<ViewerTable>createCellTableForViewerTable() {
    return new MultipleSelectionTablePanel<>(GWT.create(ConfigurationCellTableResources.class));
  }

  private void populateTable(MultipleSelectionTablePanel<ViewerTable> selectionTablePanel,
    final ViewerSchema viewerSchema) {
    selectionTablePanel.createTable(new FlowPanel(), Arrays.asList(1, 2), viewerSchema.getTables().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForShow(), 4,
        getCheckboxColumn(selectionTablePanel)),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.basicTableHeaderTableOrColumn("name"), 15,
        new TextColumn<ViewerTable>() {
          @Override
          public String getValue(ViewerTable table) {
            return table.getName();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForLabel(), 20,
        getLabelColumn()),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForDescription(), 0,
        getDescriptionColumn()));
  }

  private Column<ViewerTable, Boolean> getCheckboxColumn(MultipleSelectionTablePanel<ViewerTable> selectionTablePanel) {
    Column<ViewerTable, Boolean> column = new Column<ViewerTable, Boolean>(new CheckboxCell(false, false)) {
      @Override
      public Boolean getValue(ViewerTable viewerTable) {
        if (initialLoading.get(viewerTable.getUuid()) == null) {
          initialLoading.put(viewerTable.getUuid(), false);
          selectionTablePanel.getSelectionModel().setSelected(viewerTable,
            collectionStatus.showTable(viewerTable.getUuid()));
        }

        return selectionTablePanel.getSelectionModel().isSelected(viewerTable);
      }
    };

    column.setFieldUpdater((i, viewerTable, aBoolean) -> {
      selectionTablePanel.getSelectionModel().setSelected(viewerTable, aBoolean);
      changes = true;
    });

    return column;
  }

  private Column<ViewerTable, String> getLabelColumn() {
    Column<ViewerTable, String> label = new Column<ViewerTable, String>(new RequiredEditableCell() {}) {
      @Override
      public String getValue(ViewerTable table) {
        if (checkTableManagementDataIsEmpty(table)) {
          StatusHelper helper = new StatusHelper(collectionStatus.getTableStatus(table.getUuid()).getCustomName(),
            collectionStatus.getTableStatus(table.getUuid()).getCustomDescription());
          addToTableManagementData(table, helper);
          return collectionStatus.getTableStatus(table.getUuid()).getCustomName();
        } else {
          return getTableManagementData(table).getLabel();
        }
      }
    };

    label.setFieldUpdater((index, table, value) -> {
      if (checkTableManagementDataIsEmpty(table)) {
        StatusHelper helper = new StatusHelper(value,
          collectionStatus.getTableStatus(table.getUuid()).getCustomDescription());
        addToTableManagementData(table, helper);
      } else {
        StatusHelper data = getTableManagementData(table);
        data.setLabel(value);
        addToTableManagementData(table, data);
      }

      changes = true;
    });
    return label;
  }

  private Column<ViewerTable, String> getDescriptionColumn() {
    Column<ViewerTable, String> description = new Column<ViewerTable, String>(new TextAreaInputCell() {}) {
      @Override
      public String getValue(ViewerTable table) {
        return collectionStatus.getTableStatus(table.getUuid()).getCustomDescription();
      }
    };

    description.setFieldUpdater((index, table, value) -> {
      if (checkTableManagementDataIsEmpty(table)) {
        StatusHelper helper = new StatusHelper(collectionStatus.getTableStatus(table.getUuid()).getCustomName(), value);
        addToTableManagementData(table, helper);
      } else {
        final StatusHelper statusHelper = getTableManagementData(table);
        statusHelper.setDescription(value);
        addToTableManagementData(table, statusHelper);
      }
    });
    return description;
  }

  private boolean selectionValidation() {
    boolean result = false;

    for (MultipleSelectionTablePanel<ViewerTable> table : tables.values()) {
      boolean notEmpty = !table.getSelectionModel().getSelectedSet().isEmpty();
      result |= notEmpty;

    }

    return result;
  }

  private boolean uniquenessValidation() {
    for (String schemaUUID : tables.keySet()) {
      Set<String> uniques = new HashSet<>();

      Map<String, StatusHelper> helperMap = tableManagementData.get(schemaUUID);
      List<String> selectedSet = tables.get(schemaUUID).getSelectionModel().getSelectedSet().stream()
        .map(ViewerTable::getId).collect(Collectors.toList());

      for (String tableId : selectedSet) {
        if (!uniques.add(helperMap.get(tableId).getLabel())) {
          return false;
        }
      }
    }

    return true;
  }

  private boolean inputValidation() {
    for (String schemaUUID : tables.keySet()) {
      Map<String, StatusHelper> helperMap = tableManagementData.get(schemaUUID);
      List<String> selectedSet = tables.get(schemaUUID).getSelectionModel().getSelectedSet().stream()
        .map(ViewerTable::getId).collect(Collectors.toList());

      for (String tableId : selectedSet) {
        if (ViewerStringUtils.isBlank(helperMap.get(tableId).getLabel())) {
          return false;
        }
      }
    }

    return true;
  }

  private void addToTableManagementData(ViewerTable table, StatusHelper helper) {
    Map<String, StatusHelper> helperMap = tableManagementData.get(table.getSchemaUUID());

    if (helperMap == null) {
      helperMap = new HashMap<>();
    }

    helperMap.put(table.getId(), helper);
    tableManagementData.put(table.getSchemaUUID(), helperMap);
  }

  private boolean checkTableManagementDataIsEmpty(ViewerTable table) {
    if (tableManagementData.get(table.getSchemaUUID()) == null)
      return true;

    return tableManagementData.get(table.getSchemaUUID()).get(table.getId()) == null;
  }

  private StatusHelper getTableManagementData(ViewerTable table) {
    return tableManagementData.get(table.getSchemaUUID()).get(table.getId());
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forTableManagement(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }
}