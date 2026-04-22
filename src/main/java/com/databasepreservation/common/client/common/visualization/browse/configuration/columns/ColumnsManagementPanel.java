/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.columns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.UserLogin;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.CommonDialogs;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.cells.ActionsCell;
import com.databasepreservation.common.client.common.lists.cells.DisableableCheckboxCell;
import com.databasepreservation.common.client.common.lists.cells.RequiredEditableCell;
import com.databasepreservation.common.client.common.lists.cells.TextAreaInputCell;
import com.databasepreservation.common.client.common.lists.cells.helper.CheckboxData;
import com.databasepreservation.common.client.common.lists.columns.ButtonColumn;
import com.databasepreservation.common.client.common.lists.widgets.BasicTablePanel;
import com.databasepreservation.common.client.common.sidebar.Sidebar;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.visualization.browse.configuration.ConfigurationStatusPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.CustomizeColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual.VirtualColumnOptionsPanel;
import com.databasepreservation.common.client.configuration.observer.ICollectionStatusObserver;
import com.databasepreservation.common.client.configuration.observer.ISaveButtonObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.helpers.StatusHelper;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.services.AuthenticationService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.ConfigurationCellTableResources;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
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
public class ColumnsManagementPanel extends RightPanel implements ICollectionStatusObserver, ISaveButtonObserver {

  private static final String FA_FW = "fa-fw";
  private ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel mainHeader;
  @UiField
  FlowPanel content;
  @UiField
  Button btnGotoTable;
  @UiField
  ConfigurationStatusPanel configurationStatusPanel;

  interface ColumnsManagementPanelUiBinder extends UiBinder<Widget, ColumnsManagementPanel> {
  }

  private static ColumnsManagementPanelUiBinder binder = GWT.create(ColumnsManagementPanelUiBinder.class);

  private static Map<String, ColumnsManagementPanel> instances = new HashMap<>();

  private CollectionStatus collectionStatus;
  private ViewerDatabase database;
  private Sidebar sidebar;
  private String tableId;

  private boolean isInitialized = false;

  private Button btnSave = new Button();
  private BasicTablePanel<ColumnStatus> cellTable;
  private boolean changes = false;

  /** Local cache of user edits pending to be flushed to the CollectionStatus. */
  private Map<String, StatusHelper> editableValues = new HashMap<>();

  public static ColumnsManagementPanel getInstance(CollectionStatus status, ViewerDatabase database, String tableUUID,
    Sidebar sidebar) {
    final String value = (tableUUID == null) ? status.getFirstTableVisible() : tableUUID;
    return instances.computeIfAbsent(database.getUuid() + value,
      k -> new ColumnsManagementPanel(database, status, value, sidebar));
  }

  private ColumnsManagementPanel(ViewerDatabase database, CollectionStatus collectionStatus, String tableId,
    Sidebar sidebar) {
    initWidget(binder.createAndBindUi(this));
    ObserverManager.getCollectionObserver().addObserver(this);
    ObserverManager.getSaveObserver().addObserver(this);

    this.database = database;
    this.collectionStatus = collectionStatus;
    this.tableId = tableId;
    this.sidebar = sidebar;
    configurationStatusPanel.setDatabase(database);

    initStaticElements();
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    if (!isInitialized) {
      fetchProjectedDatabase();
    }
  }

  /**
   * Retrieves the unified projection schema from the backend and triggers a UI
   * rebuild.
   */
  private void fetchProjectedDatabase() {
    ConfigurationStateController.getInstance().fetchProjectedDatabase(database.getUuid(), (projectedDb, status) -> {
      this.database = projectedDb;
      this.collectionStatus = status;
      this.isInitialized = true;

      configurationStatusPanel.setDatabase(this.database);

      if (this.sidebar != null) {
        this.sidebar.reset(this.database, this.collectionStatus);
      }

      rebuildUI();
    });
  }

  /**
   * Initializes static UI elements (Headers, static buttons) that do not depend
   * on dynamic schema projection.
   */
  private void initStaticElements() {
    TableStatus table = collectionStatus.getTableStatusByTableId(tableId);
    if (table == null)
      return;

    mainHeader.insert(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE),
      table.getCustomName(), "h1"), 0);
    mainHeader.setTitle(table.getCustomDescription());

    MetadataField instance = MetadataField.createInstance(messages.columnManagementPageDescription());
    instance.setCSS("table-row-description", "font-size-description");
    content.add(instance);

    btnGotoTable.setText(messages.dataTransformationBtnBrowseTable());
    btnGotoTable.addClickHandler(e -> HistoryManager.gotoTable(database.getUuid(), tableId));

    configureButtonsPanel(table);
  }

  private void rebuildUI() {
    final TableStatus table = collectionStatus.getTableStatusByTableId(tableId);

    if (table == null || !table.isShow()) {
      HistoryManager.gotoAdvancedConfiguration(database.getUuid());
      return;
    }

    UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
      @Override
      public void onSuccess(User user) {
        AuthenticationService.Util.call((Boolean authenticationIsEnabled) -> {
          // Safely remove previous table instance
          if (cellTable != null) {
            content.remove(cellTable);
          }

          if (!authenticationIsEnabled || user.isAdmin()) {
            cellTable = populateTableAdmin(table);
          } else {
            cellTable = populateTableUser(table);
          }

          // Insert back right below the description field
          content.insert(cellTable, 2);
        }).isAuthenticationEnabled();
      }
    });
  }

  private void configureButtonsPanel(TableStatus table) {
    Button btnCancel = new Button();
    btnCancel.setText(messages.basicActionCancel());
    btnCancel.addStyleName("btn btn-danger btn-times-circle");
    btnCancel.addClickHandler(e -> handleCancelEvent(changes));

    btnSave.setText(messages.basicActionSave());
    btnSave.addStyleName("btn btn-primary btn-save");
    btnSave.addClickHandler(e -> saveChanges(changes));

    instances.forEach((key, object) -> {
      if (key.startsWith(database.getUuid())) {
        btnSave.setEnabled(object.btnSave.isEnabled());
      }
    });

    Button btnAddVirtualColumn = getBtnAddVirtualColumn();

    content.add(CommonClientUtils.wrapOnDiv("navigation-panel-buttons",
      CommonClientUtils.wrapOnDiv("btn-item", btnSave), CommonClientUtils.wrapOnDiv("btn-item", btnCancel),
      CommonClientUtils.wrapOnDiv("btn-item", btnAddVirtualColumn)));
  }

  @NotNull
  private Button getBtnAddVirtualColumn() {
    Button btnAddVirtualColumn = new Button();
    btnAddVirtualColumn.setText(messages.columnManagementButtonTextForAddVirtualColumn());
    btnAddVirtualColumn.addStyleName("btn btn-primary btn-plus");

    btnAddVirtualColumn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        TableStatus currentTableStatus = collectionStatus.getTableStatusByTableId(tableId);
        VirtualColumnOptionsPanel virtualColumnOptionsPanel = VirtualColumnOptionsPanel
          .createInstance(currentTableStatus, new ColumnStatus());

        Dialogs.showDialogColumnConfiguration(messages.columnManagementLabelForVirtualColumn(), "600px",
          messages.basicActionAdd(), messages.basicActionCancel(), Arrays.asList(virtualColumnOptionsPanel),
          new DefaultAsyncCallback<Dialogs.DialogAction>() {
            @Override
            public void onSuccess(Dialogs.DialogAction value) {
              if (value.equals(Dialogs.DialogAction.SAVE)) {
                ColumnStatus columnStatus = virtualColumnOptionsPanel.getColumnStatus();
                boolean alreadyExists = currentTableStatus.getColumns().stream()
                  .anyMatch(c -> c.getId().equals(columnStatus.getId()));

                if (!alreadyExists) {
                  int maxIndex = currentTableStatus.getColumns().stream().mapToInt(ColumnStatus::getColumnIndex).max()
                    .orElse(-1);
                  columnStatus.setColumnIndex(maxIndex + 1);

                  currentTableStatus.addColumnStatus(columnStatus);
                  collectionStatus.setNeedsToBeProcessed(true);

                  // Pushes intention to backend, fetches projection, and redraws UI automatically
                  saveChanges(true);
                }
              }
            }
          });
      }
    });
    return btnAddVirtualColumn;
  }

  /**
   * Translates UI edits (cached in editableValues) into the persistent
   * CollectionStatus object. Iterates over the memory state, NOT the visual grid,
   * to avoid wiping programmatically added columns.
   */
  private void applyEditableValuesToState() {
    TableStatus tableStatus = collectionStatus.getTableStatusByTableId(tableId);
    if (tableStatus == null || tableStatus.getColumns() == null)
      return;

    for (ColumnStatus column : tableStatus.getColumns()) {
      StatusHelper helper = editableValues.get(column.getId());
      if (helper != null) {
        column.setCustomDescription(helper.getDescription());
        column.setCustomName(helper.getLabel());
        column.updateTableShowValue(helper.isShowInTable());
        column.updateDetailsShowValue(helper.isShowInDetails());
        column.updateAdvancedSearchShowValue(helper.isShowInAdvancedSearch());
      }
    }
  }

  private void saveChanges(boolean hasChanges) {
    if (!hasChanges) {
      Toast.showInfo(messages.columnManagementPageTitle(), messages.columnManagementPageToastDescription());
    } else if (!validateUniqueInputs()) {
      Dialogs.showErrors(messages.columnManagementPageTitle(), messages.columnManagementPageDialogErrorUnique(),
        messages.basicActionClose());
    } else if (!validateCheckboxes()) {
      Dialogs.showErrors(messages.columnManagementPageTitle(), messages.columnManagementPageDialogErrorDescription(),
        messages.basicActionClose());
    } else {
      executeSaveChanges();
    }
  }

  private void executeSaveChanges() {
    flushEditableValuesToState();

    UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
      @Override
      public void onSuccess(User user) {
        processUserAuthenticationForSave(user);
      }
    });
  }

  // Flushes local UI edits to the status object securely
  private void flushEditableValuesToState() {
    instances.forEach((key, object) -> {
      if (key.startsWith(database.getUuid())) {
        object.applyEditableValuesToState();
      }
    });
  }

  private void processUserAuthenticationForSave(User user) {
    AuthenticationService.Util.call((Boolean authEnabled) -> {
      if (!authEnabled || user.isAdmin()) {
        updateConfigurationContext();
      } else {
        updateCollectionCustomizeProperties();
      }
    }).isAuthenticationEnabled();
  }

  private void updateConfigurationContext() {
    ConfigurationStateController.getInstance().updateConfigurationContext(database.getUuid(), collectionStatus,
      (projectedDb, status) -> {
        this.database = projectedDb;
        this.collectionStatus = status;
        configurationStatusPanel.setDatabase(this.database);
        ObserverManager.getCollectionObserver().setCollectionStatus(this.collectionStatus);

        if (sidebar != null) {
          sidebar.reset(database, collectionStatus);
        }

        changes = false;
        isInitialized = true;
        Toast.showInfo(messages.columnManagementPageTitle(), messages.columnManagementPageToastDescription());

        rebuildUI();
      }, (message, details) -> {
        Dialogs.showConfigurationDependencyErrors(message, details, messages.basicActionClose());
      });
  }

  private void updateCollectionCustomizeProperties() {
    ConfigurationStateController.getInstance().updateCollectionCustomizeProperties(database.getUuid(), collectionStatus,
      () -> {
        ObserverManager.getCollectionObserver().setCollectionStatus(collectionStatus);

        if (sidebar != null) {
          sidebar.reset(database, collectionStatus);
        }

        changes = false;
        isInitialized = true;
        Toast.showInfo(messages.columnManagementPageTitle(), messages.columnManagementPageToastDescription());
        rebuildUI();
      });
  }

  private boolean validateUniqueInputs() {
    boolean isUnique = true;
    for (Map.Entry<String, ColumnsManagementPanel> entry : instances.entrySet()) {
      if (isUnique && entry.getKey().startsWith(database.getUuid())) {
        Set<String> uniques = new HashSet<>();
        for (StatusHelper value : entry.getValue().editableValues.values()) {
          if (isUnique && !uniques.add(value.getLabel())) {
            isUnique = false;
          }
        }
      }
    }
    return isUnique;
  }

  private boolean validateCheckboxes() {
    boolean isValid = true;
    for (Map.Entry<String, ColumnsManagementPanel> entry : instances.entrySet()) {
      if (isValid && entry.getKey().startsWith(database.getUuid())) {
        int countDetails = 0, countTable = 0;
        for (StatusHelper helper : entry.getValue().editableValues.values()) {
          if (!helper.isShowInDetails())
            countDetails++;
          if (!helper.isShowInTable())
            countTable++;
        }
        TableStatus ts = collectionStatus.getTableStatusByTableId(tableId);
        if (ts != null && (ts.getColumns().size() - countDetails <= 0 || ts.getColumns().size() - countTable <= 0)) {
          isValid = false;
        }
      }
    }
    return isValid;
  }

  private BasicTablePanel<ColumnStatus> populateTableAdmin(final TableStatus tableStatus) {
    Collections.sort(tableStatus.getColumns());
    BasicTablePanel<ColumnStatus> tablePanel = new BasicTablePanel<>(new FlowPanel(), SafeHtmlUtils.EMPTY_SAFE_HTML,
      GWT.create(ConfigurationCellTableResources.class), tableStatus.getColumns().iterator(),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderOrder(), 6, getOrderColumn()),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderTableOrColumn("name"), 0,
        new Column<ColumnStatus, SafeHtml>(new SafeHtmlCell()) {
          @Override
          public SafeHtml getValue(ColumnStatus column) {
            return renderColumnCell(column);
          }
        }),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderLabel(), 15, getLabelColumn()),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderDescription(), 0, getDescriptionColumn()),
      new BasicTablePanel.ColumnInfo<>(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
        .getTagWithStyleName(FontAwesomeIconManager.PAINT_BRUSH, messages.basicTableHeaderOptions(), FA_FW)), false, 3,
        getTableCustomizationColumn()),
      new BasicTablePanel.ColumnInfo<>(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
        .getTagWithStyleName(FontAwesomeIconManager.COG, messages.basicTableHeaderOptions(), FA_FW)), false, 3,
        getOptionsColumn()),
      new BasicTablePanel.ColumnInfo<>(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
        .getTagWithStyleName(FontAwesomeIconManager.TABLE, messages.columnManagementPageTooltipForTable(), FA_FW)),
        false, 3, getTableCheckboxColumn()),
      new BasicTablePanel.ColumnInfo<>(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
        .getTagWithStyleName(FontAwesomeIconManager.LIST, messages.columnManagementPageTooltipForDetails(), FA_FW)),
        false, 3, getDetailsCheckboxColumn()),
      new BasicTablePanel.ColumnInfo<>(
        SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTagWithStyleName(FontAwesomeIconManager.SEARCH_PLUS,
          messages.columnManagementPageTooltipForAdvancedSearch(), FA_FW)),
        false, 3, getAdvancedSearchCheckboxColumn()));

    tablePanel.getSelectionModel().addSelectionChangeHandler(event -> {
      final ColumnStatus selected = tablePanel.getSelectionModel().getSelectedObject();
      if (selected != null) {
        tablePanel.getSelectionModel().clear();
      }
    });

    return tablePanel;
  }

  private BasicTablePanel<ColumnStatus> populateTableUser(final TableStatus tableStatus) {
    Collections.sort(tableStatus.getColumns());
    BasicTablePanel<ColumnStatus> tablePanel = new BasicTablePanel<>(new FlowPanel(), SafeHtmlUtils.EMPTY_SAFE_HTML,
      GWT.create(ConfigurationCellTableResources.class), tableStatus.getColumns().iterator(),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderTableOrColumn("name"), 0,
        new Column<ColumnStatus, SafeHtml>(new SafeHtmlCell()) {
          @Override
          public SafeHtml getValue(ColumnStatus column) {
            return renderColumnCell(column);
          }
        }),
      new BasicTablePanel.ColumnInfo<>(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
        .getTagWithStyleName(FontAwesomeIconManager.PAINT_BRUSH, messages.basicTableHeaderOptions(), FA_FW)), false, 3,
        getTableCustomizationColumn()));

    tablePanel.getSelectionModel().addSelectionChangeHandler(event -> {
      final ColumnStatus selected = tablePanel.getSelectionModel().getSelectedObject();
      if (selected != null) {
        tablePanel.getSelectionModel().clear();
      }
    });

    return tablePanel;
  }

  @NotNull
  private static SafeHtml renderColumnCell(ColumnStatus column) {
    SafeHtmlBuilder sb = new SafeHtmlBuilder();
    if (column.isVirtual()) {
      ProcessingState state = (column.getVirtualColumnStatus() != null)
        ? column.getVirtualColumnStatus().getProcessingState()
        : null;
      sb.appendHtmlConstant("<span>").append(SafeHtmlUtils.fromString(column.getName())).appendHtmlConstant("</span>");

      if (ProcessingState.TO_REMOVE.equals(state)) {
        sb.appendHtmlConstant("<span style='color: #dc3545; margin-left: 5px;'>")
          .appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.MINUS_CIRCLE))
          .appendHtmlConstant("</span>");
      } else if (ProcessingState.TO_PROCESS.equals(state)) {
        sb.appendHtmlConstant("<span style='color: #28a745; margin-left: 5px;'>")
          .appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.PLUS_CIRCLE))
          .appendHtmlConstant("</span>");
      } else if (ProcessingState.PROCESSING.equals(state)) {
        sb.appendHtmlConstant("<span style='color: #ffc107; margin-left: 5px;'>")
          .appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.LOADING))
          .appendHtmlConstant("</span>");
      } else {
        sb.appendHtmlConstant("<span style='color: #007bff; margin-left: 5px;'>")
          .appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.CLONE))
          .appendHtmlConstant("</span>");
      }
      return sb.toSafeHtml();
    } else if (column.getType() != null && column.getType().equals(ViewerType.dbTypes.NESTED)) {
      sb.appendHtmlConstant("<span class=\"table-ref-link\">");
      if (column.getNestedColumns().getPath() != null) {
        sb.appendHtmlConstant(column.getNestedColumns().getPath());
      }
      sb.appendHtmlConstant("<span class=\"table-ref-path\"><b>")
        .appendHtmlConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.COLUMN));
      if (column.getNestedColumns().getNestedFields() != null) {
        sb.appendEscaped(String.join(", ", column.getNestedColumns().getNestedFields()));
      }
      sb.appendHtmlConstant("</b></span></span>");
      return sb.toSafeHtml();
    } else {
      return SafeHtmlUtils.fromString(column.getName());
    }
  }

  // ==============================================================================================
  // COLUMN OPTIONS & DIALOGS
  // ==============================================================================================

  /**
   * Evaluates the column type and delegates the dialog creation to specific
   * private methods.
   */
  private Column<ColumnStatus, String> getOptionsColumn() {
    Column<ColumnStatus, String> options = new ButtonColumn<ColumnStatus>() {
      @Override
      public void render(Cell.Context context, ColumnStatus object, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant(
          "<div class=\"center-cell\"><button class=\"btn btn-cell-action\" type=\"button\" tabindex=\"-1\"><i class=\"fa fa-cog\"></i></button></div>");
      }

      @Override
      public String getValue(ColumnStatus object) {
        return messages.basicActionOpen();
      }
    };

    options.setFieldUpdater((index, columnStatus, value) -> {
      TableStatus tableStatus = collectionStatus.getTableStatusByTableId(tableId);

      // Delegation to Factory prevents OCP violations in the main UI Panel
      ColumnDialogFactory.openDialog(database, collectionStatus, tableStatus, columnStatus, messages,
        this::saveChanges);
    });

    return options;
  }

  private Column<ColumnStatus, ColumnStatus> getOrderColumn() {
    List<HasCell<ColumnStatus, ?>> cells = new ArrayList<>();
    cells
      .add(new ActionsCell<>(messages.columnManagementPageTextForArrowUp(), FontAwesomeIconManager.ARROW_UP, object -> {
        List<ColumnStatus> list = cellTable.getDataProvider().getList();
        if (object.getOrder() != 1) {
          updateColumnOrder(list, object.getOrder() - 2, object.getOrder() - 1);
          cellTable.getDataProvider().setList(list);
          cellTable.getDataProvider().refresh();
        }
      }));
    cells.add(
      new ActionsCell<>(messages.columnManagementPageTextForArrowDown(), FontAwesomeIconManager.ARROW_DOWN, object -> {
        List<ColumnStatus> list = cellTable.getDataProvider().getList();
        if (object.getOrder() != collectionStatus.getTableStatusByTableId(tableId).getColumns().size()) {
          updateColumnOrder(list, object.getOrder(), object.getOrder() - 1);
          cellTable.getDataProvider().setList(list);
          cellTable.getDataProvider().refresh();
        }
      }));

    return new Column<ColumnStatus, ColumnStatus>(new CompositeCell<>(cells)) {
      @Override
      public ColumnStatus getValue(ColumnStatus columnStatus) {
        return columnStatus;
      }
    };
  }

  private Column<ColumnStatus, Boolean> getTableCheckboxColumn() {
    Column<ColumnStatus, Boolean> checkbox = new Column<ColumnStatus, Boolean>(new CheckboxCell(true, true)) {
      @Override
      public Boolean getValue(ColumnStatus column) {
        StatusHelper helper = editableValues.computeIfAbsent(column.getId(),
          k -> new StatusHelper(column.getCustomName(), column.getCustomDescription(),
            column.getSearchStatus().getList().isShow(), column.getDetailsStatus().isShow(),
            column.getSearchStatus().getAdvanced().isFixed(),
            column.getSearchStatus().getList().getCustomizeProperties()));
        return helper.isShowInTable();
      }
    };
    checkbox.setFieldUpdater((index, column, value) -> {
      editableValues.get(column.getId()).setShowInTable(value);
      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });
    return checkbox;
  }

  private Column<ColumnStatus, Boolean> getDetailsCheckboxColumn() {
    Column<ColumnStatus, Boolean> checkbox = new Column<ColumnStatus, Boolean>(new CheckboxCell(true, true)) {
      @Override
      public Boolean getValue(ColumnStatus column) {
        StatusHelper helper = editableValues.computeIfAbsent(column.getId(),
          k -> new StatusHelper(column.getCustomName(), column.getCustomDescription(),
            column.getSearchStatus().getList().isShow(), column.getDetailsStatus().isShow(),
            column.getSearchStatus().getAdvanced().isFixed(),
            column.getSearchStatus().getList().getCustomizeProperties()));
        return helper.isShowInDetails();
      }
    };
    checkbox.setFieldUpdater((index, column, value) -> {
      editableValues.get(column.getId()).setShowInDetails(value);
      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });
    return checkbox;
  }

  private Column<ColumnStatus, CheckboxData> getAdvancedSearchCheckboxColumn() {
    Column<ColumnStatus, CheckboxData> checkbox = new Column<ColumnStatus, CheckboxData>(
      new DisableableCheckboxCell(false, true)) {
      @Override
      public CheckboxData getValue(ColumnStatus column) {
        CheckboxData data = new CheckboxData();
        data.setDisable(column.getType() != null && column.getType().equals(ViewerType.dbTypes.BINARY));
        StatusHelper helper = editableValues.computeIfAbsent(column.getId(),
          k -> new StatusHelper(column.getCustomName(), column.getCustomDescription(),
            column.getSearchStatus().getList().isShow(), column.getDetailsStatus().isShow(),
            column.getSearchStatus().getAdvanced().isFixed(),
            column.getSearchStatus().getList().getCustomizeProperties()));
        data.setChecked(helper.isShowInAdvancedSearch());
        return data;
      }
    };
    checkbox.setFieldUpdater((index, column, value) -> {
      editableValues.get(column.getId()).setShowInAdvancedSearch(value.isChecked());
      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });
    return checkbox;
  }

  private Column<ColumnStatus, String> getLabelColumn() {
    Column<ColumnStatus, String> label = new Column<ColumnStatus, String>(new RequiredEditableCell("") {
      @Override
      public void onBrowserEvent(Context context, Element parent, String value, NativeEvent event,
        ValueUpdater<String> valueUpdater) {
        if (BrowserEvents.KEYUP.equals(event.getType())) {
          InputElement input = getInputElement(parent);
          if (ViewerStringUtils.isBlank(input.getValue())) {
            btnSave.setEnabled(false);
            ObserverManager.getSaveObserver().setEnabled(database.getUuid(), false);
          } else {
            btnSave.setEnabled(true);
            ObserverManager.getSaveObserver().setEnabled(database.getUuid(), true);
          }
        }
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
      }
    }) {
      @Override
      public String getValue(ColumnStatus column) {
        StatusHelper helper = editableValues.computeIfAbsent(column.getId(),
          k -> new StatusHelper(column.getCustomName(), column.getCustomDescription(),
            column.getSearchStatus().getList().isShow(), column.getDetailsStatus().isShow(),
            column.getSearchStatus().getAdvanced().isFixed(),
            column.getSearchStatus().getList().getCustomizeProperties()));
        return helper.getLabel();
      }
    };
    label.setFieldUpdater((index, column, value) -> {
      editableValues.get(column.getId()).setLabel(value);
      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });
    return label;
  }

  private Column<ColumnStatus, String> getDescriptionColumn() {
    Column<ColumnStatus, String> description = new Column<ColumnStatus, String>(new TextAreaInputCell() {}) {
      @Override
      public String getValue(ColumnStatus column) {
        StatusHelper helper = editableValues.computeIfAbsent(column.getId(),
          k -> new StatusHelper(column.getCustomName(), column.getCustomDescription(),
            column.getSearchStatus().getList().isShow(), column.getDetailsStatus().isShow(),
            column.getSearchStatus().getAdvanced().isFixed(),
            column.getSearchStatus().getList().getCustomizeProperties()));
        return helper.getDescription();
      }
    };
    description.setFieldUpdater((index, column, value) -> {
      editableValues.get(column.getId()).setDescription(value);
      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });
    return description;
  }

  private Column<ColumnStatus, String> getTableCustomizationColumn() {
    Column<ColumnStatus, String> option = new ButtonColumn<ColumnStatus>() {
      @Override
      public String getValue(ColumnStatus columnStatus) {
        return messages.basicActionOpen();
      }

      @Override
      public void render(Cell.Context context, ColumnStatus object, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant(
          "<div class=\"center-cell\"><button class=\"btn btn-cell-action\" type=\"button\" tabindex=\"-1\"><i class=\"fas fa-paint-brush\"></i></button></div>");
      }
    };
    option.setFieldUpdater((index, columnStatus, value) -> {
      CustomizeColumnOptionsPanel optionsPanel = CustomizeColumnOptionsPanel.createInstance(columnStatus);
      Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), "300px", messages.basicActionSave(),
        messages.basicActionCancel(), optionsPanel, new DefaultAsyncCallback<Dialogs.DialogAction>() {
          @Override
          public void onSuccess(Dialogs.DialogAction value) {
            if (value.equals(Dialogs.DialogAction.SAVE)) {
              if (optionsPanel.validate()) {
                collectionStatus.getColumnByTableIdAndColumn(tableId, columnStatus.getId())
                  .updateCustomizeProperties(optionsPanel.getProperties());
                saveChanges(true);
              } else {
                Dialogs.showErrors(messages.columnManagementPageTitle(),
                  messages.columnManagementPageDialogErrorValueMustBeAnInteger(), messages.basicActionClose());
              }
            }
          }
        });
    });
    return option;
  }

  private void updateColumnOrder(List<ColumnStatus> list, int relativeToClickIndex, int clickedIndex) {
    ColumnStatus relative = list.get(relativeToClickIndex);
    ColumnStatus clicked = list.get(clickedIndex);
    int relativeOrder = relative.getOrder();
    int clickedOrder = clicked.getOrder();
    relative.setOrder(clickedOrder);
    clicked.setOrder(relativeOrder);
    list.set(clickedIndex, relative);
    list.set(relativeToClickIndex, clicked);
    changes = true;
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
              sidebar.reset(database, collectionStatus);
              HistoryManager.gotoAdvancedConfiguration(database.getUuid());
            }
          }
        });
    } else {
      HistoryManager.gotoAdvancedConfiguration(database.getUuid());
    }
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forColumnsManagement(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  /**
   * Observer callback triggered when structural changes occur globally. Flags
   * panel as dirty. If currently attached, immediately fetches the new
   * projection.
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

  @Override
  public void update(String databaseUUID, boolean enabled) {
    if (database.getUuid().equals(databaseUUID)) {
      btnSave.setEnabled(enabled);
    }
  }
}
