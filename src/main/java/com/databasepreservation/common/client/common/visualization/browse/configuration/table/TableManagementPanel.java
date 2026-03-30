/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

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
import com.databasepreservation.common.client.common.lists.columns.ButtonColumn;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.visualization.browse.configuration.ConfigurationStatusPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.ConfigurationStateController;
import com.databasepreservation.common.client.configuration.observer.ICollectionStatusObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.helpers.StatusHelper;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.ConfigurationCellTableResources;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TableManagementPanel extends ContentPanel implements ICollectionStatusObserver {
  private ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel header;

  @UiField
  FlowPanel content;

  @UiField
  ConfigurationStatusPanel configurationStatusPanel;

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

  private boolean isInitialized = false;

  /**
   * Tracks dynamically injected widgets to safely remove them during rebuilds
   * without killing native UiBinder nodes.
   */
  private List<Widget> dynamicWidgets = new ArrayList<>();

  public static TableManagementPanel getInstance(ViewerDatabase database, CollectionStatus status) {
    return instances.computeIfAbsent(database.getUuid(), k -> new TableManagementPanel(database, status));
  }

  private TableManagementPanel(ViewerDatabase database, CollectionStatus collectionStatus) {
    initWidget(binder.createAndBindUi(this));
    ObserverManager.getCollectionObserver().addObserver(this);
    this.database = database;
    this.collectionStatus = collectionStatus;
    configurationStatusPanel.setDatabase(database);
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    if (!isInitialized) {
      fetchProjectedDatabase();
    }
  }

  /**
   * Fetches the unified configuration context (State + Projection) from the
   * backend.
   */
  private void fetchProjectedDatabase() {
    ConfigurationStateController.getInstance().fetchProjectedDatabase(database.getUuid(), (projectedDb, status) -> {
      this.database = projectedDb;
      this.collectionStatus = status;
      this.isInitialized = true;

      configurationStatusPanel.setDatabase(this.database);
      rebuildUI();
    });
  }

  /**
   * Safely clears only dynamically appended elements and triggers a complete UI
   * rebuild based on the current projected schema.
   */
  private void rebuildUI() {
    // Safely remove only the dynamic widgets to prevent wiping the
    // ConfigurationStatusPanel from the DOM
    for (Widget w : dynamicWidgets) {
      w.removeFromParent();
    }
    dynamicWidgets.clear();

    tables.clear();
    initialLoading.clear();
    tableManagementData.clear();

    init();
  }

  private void init() {
    // 1. Header
    Widget headerHtml = CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE),
      messages.tableManagementPageTitle(), "h1");
    header.add(headerHtml);
    dynamicWidgets.add(headerHtml);

    MetadataField instance = MetadataField.createInstance(messages.tableManagementPageTableTextForDescription());
    instance.setCSS("table-row-description", "font-size-description");
    content.add(instance);
    dynamicWidgets.add(instance);

    // 2. Data Grids
    for (ViewerSchema schema : database.getMetadata().getSchemas()) {
      MultipleSelectionTablePanel<ViewerTable> schemaTable = createCellTableForViewerTable();
      schemaTable.setHeight("100%");
      populateTable(schemaTable, database.getMetadata().getSchema(schema.getUuid()));
      tables.put(schema.getUuid(), schemaTable);

      FlowPanel widgets = CommonClientUtils.wrapOnDiv("table-management-schema-divider",
        CommonClientUtils.getHeader(SafeHtmlUtils.fromSafeConstant(schema.getName()), "table-management-schema-title"),
        schemaTable);
      content.add(widgets);
      dynamicWidgets.add(widgets);
    }

    // 3. Action Buttons
    configureButtonsPanel();
  }

  private void configureButtonsPanel() {
    Button btnCancel = new Button();
    btnCancel.setText(messages.basicActionCancel());
    btnCancel.addStyleName("btn btn-danger btn-times-circle");
    btnSave.setText(messages.basicActionSave());
    btnSave.addStyleName("btn btn-primary btn-save");

    btnCancel.addClickHandler(e -> handleCancelEvent(changes));
    btnSave.addClickHandler(clickEvent -> processTableManagementSave());

    Button btnAddVirtualTable = new Button();
    btnAddVirtualTable.setText(messages.tableManagementButtonTextForAddVirtualTable());
    btnAddVirtualTable.addStyleName("btn btn-primary btn-plus");

    btnAddVirtualTable.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {

        VirtualTableOptionsPanel virtualTableOptionsPanel = VirtualTableOptionsPanel.createInstance(database,
          collectionStatus, null);

        Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), "600px", messages.basicActionAdd(),
          messages.basicActionCancel(), Arrays.asList(virtualTableOptionsPanel),
          new DefaultAsyncCallback<Dialogs.DialogAction>() {
            @Override
            public void onSuccess(Dialogs.DialogAction value) {
              if (value != Dialogs.DialogAction.CANCEL) {
                addVirtualTable(virtualTableOptionsPanel, value);
              }
            }
          });
      }
    });

    FlowPanel buttonsContainer = CommonClientUtils.wrapOnDiv("navigation-panel-buttons",
      CommonClientUtils.wrapOnDiv("btn-item", btnSave), CommonClientUtils.wrapOnDiv("btn-item", btnCancel),
      CommonClientUtils.wrapOnDiv("btn-item", btnAddVirtualTable));

    content.add(buttonsContainer);
    dynamicWidgets.add(buttonsContainer);
  }

  private void processTableManagementSave() {
    if (!selectionValidation()) {
      Dialogs.showErrors(messages.tableManagementPageTitle(), messages.tableManagementPageDialogSelectionError(),
        messages.basicActionClose());
    } else if (!inputValidation()) {
      Dialogs.showErrors(messages.tableManagementPageTitle(), messages.tableManagementPageDialogInputError(),
        messages.basicActionClose());
    } else if (!uniquenessValidation()) {
      Dialogs.showErrors(messages.tableManagementPageTitle(), messages.tableManagementPageDialogUniqueError(),
        messages.basicActionClose());
    } else {
      applyTableManagementChanges();
    }
  }

  private void applyTableManagementChanges() {
    for (MultipleSelectionTablePanel<ViewerTable> value : tables.values()) {
      updateTableConditions(value);
    }
    updateCollectionStatus(collectionStatus);
  }

  private void updateTableConditions(MultipleSelectionTablePanel<ViewerTable> value) {
    for (ViewerTable table : database.getMetadata().getTables().values()) {
      collectionStatus.updateTableShowCondition(table.getUuid(), value.getSelectionModel().isSelected(table));
      updateCustomTableDataIfPresent(table);
    }
  }

  private void updateCustomTableDataIfPresent(ViewerTable table) {
    if (!checkTableManagementDataIsEmpty(table)) {
      StatusHelper statusData = getTableManagementData(table);
      collectionStatus.updateTableCustomDescription(table.getUuid(), statusData.getDescription());
      collectionStatus.updateTableCustomName(table.getUuid(), statusData.getLabel());
    }
  }

  /**
   * Pushes the mutation to the backend and synchronizes the local state with the
   * newly projected schema, immediately reflecting structural changes.
   */
  private void updateCollectionStatus(CollectionStatus newCollectionStatus) {
    ConfigurationStateController.getInstance().updateConfigurationContext(database.getUuid(), newCollectionStatus,
      (projectedDb, status) -> {
        this.database = projectedDb;
        this.collectionStatus = status;

        configurationStatusPanel.setDatabase(this.database);
        ObserverManager.getCollectionObserver().setCollectionStatus(this.collectionStatus);

        Toast.showInfo(messages.tableManagementPageTitle(), messages.tableManagementPageToastDescription());
        changes = false;
        this.isInitialized = true;

        rebuildUI();
      }, (message, details) -> {
        Dialogs.showConfigurationDependencyErrors(message, details, messages.basicActionClose());
      });
  }

  /**
   * Registers a structural intention within the configuration payload.
   */
  private void addVirtualTable(VirtualTableOptionsPanel optionsPanel, Dialogs.DialogAction action) {
    TableStatus tableStatus = optionsPanel.getTableStatus();

    if (action == Dialogs.DialogAction.SAVE) {
      tableStatus.getVirtualTableStatus().setProcessingState(ProcessingState.TO_PROCESS);
    } else {
      tableStatus.getVirtualTableStatus().setProcessingState(ProcessingState.TO_REMOVE);
    }

    collectionStatus.getTables().removeIf(t -> t.getId().equals(tableStatus.getId()));
    collectionStatus.getTables().add(tableStatus);
    collectionStatus.setNeedsToBeProcessed(true);
    collectionStatus.updateTableShowCondition(tableStatus.getUuid(), true);

    updateCollectionStatus(collectionStatus);
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

  private MultipleSelectionTablePanel<ViewerTable> createCellTableForViewerTable() {
    return new MultipleSelectionTablePanel<>(GWT.create(ConfigurationCellTableResources.class));
  }

  private void populateTable(MultipleSelectionTablePanel<ViewerTable> selectionTablePanel,
    final ViewerSchema viewerSchema) {
    selectionTablePanel.createTable(new FlowPanel(), Arrays.asList(1, 2), viewerSchema.getTables().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForShow(), 4,
        getCheckboxColumn(selectionTablePanel)),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.basicTableHeaderTableOrColumn("name"), 15, getTextColumn()),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForLabel(), 20,
        getLabelColumn()),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForDescription(), 0,
        getDescriptionColumn()),
      new MultipleSelectionTablePanel.ColumnInfo<>(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
        .getTagWithStyleName(FontAwesomeIconManager.COG, messages.basicTableHeaderOptions(), "fa-fw")), 4,
        getOptionsColumn()));
  }

  @NotNull
  private Column<ViewerTable, SafeHtml> getTextColumn() {
    return new Column<ViewerTable, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(ViewerTable table) {
        TableStatus tableStatus = collectionStatus.getTableStatus(table.getUuid());
        SafeHtmlBuilder sb = new SafeHtmlBuilder();

        sb.appendHtmlConstant("<span>");
        sb.append(SafeHtmlUtils.fromString(table.getName()));
        sb.appendHtmlConstant("</span>");

        if (tableStatus != null && tableStatus.getVirtualTableStatus() != null) {
          ProcessingState state = tableStatus.getVirtualTableStatus().getProcessingState();
          if (ProcessingState.TO_REMOVE.equals(state)) {
            sb.appendHtmlConstant("<span style='color: #dc3545; margin-left: 5px;'>");
            sb.appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.MINUS_CIRCLE));
            sb.appendHtmlConstant("</span>");
          } else if (ProcessingState.TO_PROCESS.equals(state)) {
            sb.appendHtmlConstant("<span style='color: #28a745; margin-left: 5px;'>");
            sb.appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.PLUS_CIRCLE));
            sb.appendHtmlConstant("</span>");
          } else if (ProcessingState.PROCESSING.equals(state)) {
            sb.appendHtmlConstant("<span style='color: #ffc107; margin-left: 5px;'>");
            sb.appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.LOADING));
            sb.appendHtmlConstant("</span>");
          } else {
            sb.appendHtmlConstant("<span style='color: #007bff; margin-left: 5px;'>");
            sb.appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.CLONE));
            sb.appendHtmlConstant("</span>");
          }
        }
        return sb.toSafeHtml();
      }
    };
  }

  private Column<ViewerTable, String> getOptionsColumn() {
    Column<ViewerTable, String> options = new ButtonColumn<ViewerTable>() {
      @Override
      public void render(Cell.Context context, ViewerTable viewerTable, SafeHtmlBuilder sb) {
        if (isAnOptionColumn(collectionStatus.getTableStatusByTableId(viewerTable.getId()))) {
          sb.appendHtmlConstant(
            "<div class=\"center-cell\"><button class=\"btn btn-cell-action\" type=\"button\" tabindex=\"-1\"><i class=\"fa fa-cog\"></i></button></div>");
        } else {
          sb.appendHtmlConstant(
            "<div class=\"center-cell\"><button class=\"btn btn-cell-action\" type=\"button\" tabindex=\"-1\" disabled><i class=\"fa fa-cog\"></i></button></div>");
        }
      }

      @Override
      public String getValue(ViewerTable viewerTable) {
        return messages.basicActionOpen();
      }
    };

    options.setFieldUpdater((index, viewerTable, value) -> {
      TableStatus tableStatus = collectionStatus.getTableStatusByTableId(viewerTable.getId());
      if (tableStatus != null) {
        VirtualTableOptionsPanel optionsPanel = VirtualTableOptionsPanel.createInstance(database, collectionStatus,
          tableStatus);
        Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), "600px", messages.basicActionSave(),
          messages.basicActionCancel(), messages.basicActionDelete(), Arrays.asList(optionsPanel),
          new DefaultAsyncCallback<Dialogs.DialogAction>() {
            @Override
            public void onSuccess(Dialogs.DialogAction value) {
              if (value != Dialogs.DialogAction.CANCEL) {
                addVirtualTable(optionsPanel, value);
              }
            }
          });
      }
    });

    return options;
  }

  private boolean isAnOptionColumn(TableStatus tableStatus) {
    return tableStatus != null && tableStatus.getVirtualTableStatus() != null;
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
    boolean isUnique = true;
    for (String schemaUUID : tables.keySet()) {
      if (isUnique) {
        isUnique = checkSchemaUniqueness(schemaUUID);
      }
    }
    return isUnique;
  }

  private boolean checkSchemaUniqueness(String schemaUUID) {
    boolean isUnique = true;
    Set<String> uniques = new HashSet<>();

    Map<String, StatusHelper> helperMap = tableManagementData.get(schemaUUID);
    List<String> selectedSet = tables.get(schemaUUID).getSelectionModel().getSelectedSet().stream()
      .map(ViewerTable::getId).collect(Collectors.toList());

    for (String tableId : selectedSet) {
      if (isUnique) {
        if (!uniques.add(helperMap.get(tableId).getLabel())) {
          isUnique = false;
        }
      }
    }
    return isUnique;
  }

  private boolean inputValidation() {
    boolean isValid = true;
    for (String schemaUUID : tables.keySet()) {
      if (isValid) {
        isValid = checkSchemaInputValidation(schemaUUID);
      }
    }
    return isValid;
  }

  private boolean checkSchemaInputValidation(String schemaUUID) {
    boolean isValid = true;
    Map<String, StatusHelper> helperMap = tableManagementData.get(schemaUUID);
    List<String> selectedSet = tables.get(schemaUUID).getSelectionModel().getSelectedSet().stream()
      .map(ViewerTable::getId).collect(Collectors.toList());

    for (String tableId : selectedSet) {
      if (isValid) {
        if (ViewerStringUtils.isBlank(helperMap.get(tableId).getLabel())) {
          isValid = false;
        }
      }
    }
    return isValid;
  }

  private void addToTableManagementData(ViewerTable table, StatusHelper helper) {
    Map<String, StatusHelper> helperMap = tableManagementData.computeIfAbsent(table.getSchemaUUID(),
      k -> new HashMap<>());
    helperMap.put(table.getId(), helper);
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

  /**
   * Observer callback triggered by global configuration mutations. Marks the
   * internal state as dirty to force a projection sync on the next attachment.
   */
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
}
