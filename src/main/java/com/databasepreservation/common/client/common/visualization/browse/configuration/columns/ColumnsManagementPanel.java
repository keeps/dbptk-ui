package com.databasepreservation.common.client.common.visualization.browse.configuration.columns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
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
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.BinaryColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.NestedColumnOptionsPanel;
import com.databasepreservation.common.client.configuration.observer.ICollectionStatusObserver;
import com.databasepreservation.common.client.configuration.observer.ISaveButtonObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.helpers.StatusHelper;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.services.CollectionService;
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

  interface TableManagementPanelUiBinder extends UiBinder<Widget, ColumnsManagementPanel> {
  }

  private static TableManagementPanelUiBinder binder = GWT.create(TableManagementPanelUiBinder.class);

  private static Map<String, ColumnsManagementPanel> instances = new HashMap<>();
  private CollectionStatus collectionStatus;
  private ViewerDatabase database;
  private Sidebar sidebar;
  private String tableId;
  private Button btnSave = new Button();
  private BasicTablePanel<ColumnStatus> cellTable;
  private boolean changes = false;
  private Map<String, StatusHelper> editableValues = new HashMap<>();

  public static ColumnsManagementPanel getInstance(CollectionStatus status, ViewerDatabase database, String tableUUID,
    Sidebar sidebar) {
    final String value;
    if (tableUUID == null) {
      value = status.getFirstTableVisible();
    } else {
      value = tableUUID;
    }

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

    init();
  }

  private void init() {
    configureHeader();
    final TableStatus table = collectionStatus.getTableStatusByTableId(tableId);
    cellTable = populateTable(table);
    content.add(cellTable);

    configureButtonsPanel();
  }

  private void configureButtonsPanel() {
    Button btnCancel = new Button();
    btnCancel.setText(messages.basicActionCancel());
    btnCancel.addStyleName("btn btn-danger btn-times-circle");

    btnSave.setText(messages.basicActionSave());
    btnSave.addStyleName("btn btn-primary btn-save");
    // insure that every new instance of a certain database has the save button
    // enabled or disabled according to the overall status
    instances.forEach((key, object) -> {
      if (key.startsWith(database.getUuid())) {
        btnSave.setEnabled(object.btnSave.isEnabled());
      }
    });

    btnCancel.addClickHandler(e -> handleCancelEvent(changes));

    btnSave.addClickHandler(e -> handleSaveEvent(changes));
    content.add(CommonClientUtils.wrapOnDiv("navigation-panel-buttons",
      CommonClientUtils.wrapOnDiv("btn-item", btnSave), CommonClientUtils.wrapOnDiv("btn-item", btnCancel)));
  }

  private void configureHeader() {
    mainHeader.insert(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE),
      collectionStatus.getTableStatusByTableId(tableId).getCustomName(), "h1"), 0);
    mainHeader.setTitle(collectionStatus.getTableStatusByTableId(tableId).getCustomDescription());

    MetadataField instance = MetadataField.createInstance(messages.columnManagementPageDescription());
    instance.setCSS("table-row-description", "font-size-description");

    content.add(instance);

    btnGotoTable.setText(messages.dataTransformationBtnBrowseTable());
    btnGotoTable.addClickHandler(e -> HistoryManager.gotoTable(database.getUuid(), tableId));
  }

  private List<ColumnStatus> saveChanges(BasicTablePanel<ColumnStatus> cellTable,
    Map<String, StatusHelper> editableValues) {
    List<ColumnStatus> statuses = new ArrayList<>();
    for (ColumnStatus column : cellTable.getDataProvider().getList()) {
      if (editableValues.get(column.getId()) != null) {
        column.setCustomDescription(editableValues.get(column.getId()).getDescription());
        column.setCustomName(editableValues.get(column.getId()).getLabel());
        column.updateTableShowValue(editableValues.get(column.getId()).isShowInTable());
        column.updateDetailsShowValue(editableValues.get(column.getId()).isShowInDetails());
        column.updateAdvancedSearchShowValue(editableValues.get(column.getId()).isShowInAdvancedSearch());
      }
      statuses.add(column);
    }

    return statuses;
  }

  private void handleCancelEvent(boolean changes) {
    if (changes) {
      Dialogs.showConfirmDialog(messages.columnManagementPageTitle(), messages.columnManagementPageCancelEventDialog(),
        messages.basicActionDiscard(), "btn btn-danger btn-times-circle", messages.basicActionBack(), "btn btn-link",
        new DefaultAsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean aBoolean) {
            if (!aBoolean) {
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

  private void handleSaveEvent(boolean changes) {
    if (changes) {
      if (validateUniqueInputs()) {
        if (validateCheckboxes()) {
          instances.forEach((key, object) -> {
            if (key.startsWith(database.getUuid())) {
              object.collectionStatus.getTableStatusByTableId(object.tableId)
                .setColumns(saveChanges(object.cellTable, object.editableValues));
            }
          });
          saveChanges();
        } else {
          Dialogs.showErrors(messages.columnManagementPageTitle(),
            messages.columnManagementPageDialogErrorDescription(), messages.basicActionClose());
        }
      } else {
        Dialogs.showErrors(messages.columnManagementPageTitle(), messages.columnManagementPageDialogErrorUnique(),
          messages.basicActionClose());
      }
    } else {
      Toast.showInfo(messages.columnManagementPageTitle(), messages.columnManagementPageToastDescription());
    }
  }

  private BasicTablePanel<ColumnStatus> populateTable(final TableStatus tableStatus) {
    Collections.sort(tableStatus.getColumns());
    BasicTablePanel<ColumnStatus> tablePanel = new BasicTablePanel<>(new FlowPanel(), SafeHtmlUtils.EMPTY_SAFE_HTML,
      GWT.create(ConfigurationCellTableResources.class), tableStatus.getColumns().iterator(),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderOrder(), 6, getOrderColumn()),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderTableOrColumn("name"), 0,
        new Column<ColumnStatus, SafeHtml>(new SafeHtmlCell()) {
          @Override
          public SafeHtml getValue(ColumnStatus column) {
            if (column.getType().equals(ViewerType.dbTypes.NESTED)) {
              return SafeHtmlUtils.fromSafeConstant(column.getNestedColumns().getPath());
            }
            return SafeHtmlUtils.fromString(column.getName());
          }
        }),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderLabel(), 15, getLabelColumn()),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderDescription(), 0, getDescriptionColumn()),
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

    CompositeCell<ColumnStatus> compositeCell = new CompositeCell<>(cells);
    return new Column<ColumnStatus, ColumnStatus>(compositeCell) {
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
        if (editableValues.get(column.getId()) == null) {
          StatusHelper helper = new StatusHelper(column.getCustomName(), column.getCustomDescription(),
            column.getSearchStatus().getList().isShow(), column.getDetailsStatus().isShow(),
            column.getSearchStatus().getAdvanced().isFixed());
          editableValues.put(column.getId(), helper);
          return column.getSearchStatus().getList().isShow();
        } else {
          return editableValues.get(column.getId()).isShowInTable();
        }
      }
    };

    checkbox.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        StatusHelper statusHelper = editableValues.get(column.getId());
        statusHelper.setShowInTable(value);
        editableValues.replace(column.getId(), statusHelper);
      } else {
        StatusHelper helper = new StatusHelper(column.getCustomName(), column.getCustomDescription(), value,
          column.getDetailsStatus().isShow(), column.getSearchStatus().getAdvanced().isFixed());
        editableValues.put(column.getId(), helper);
      }

      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });

    return checkbox;
  }

  private Column<ColumnStatus, Boolean> getDetailsCheckboxColumn() {
    Column<ColumnStatus, Boolean> checkbox = new Column<ColumnStatus, Boolean>(new CheckboxCell(true, true)) {
      @Override
      public Boolean getValue(ColumnStatus column) {
        if (editableValues.get(column.getId()) == null) {
          StatusHelper helper = new StatusHelper(column.getCustomName(), column.getCustomDescription(),
            column.getSearchStatus().getList().isShow(), column.getDetailsStatus().isShow(),
            column.getSearchStatus().getAdvanced().isFixed());
          editableValues.put(column.getId(), helper);
          return column.getDetailsStatus().isShow();
        } else {
          return editableValues.get(column.getId()).isShowInDetails();
        }
      }
    };

    checkbox.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        StatusHelper statusHelper = editableValues.get(column.getId());
        statusHelper.setShowInDetails(value);
        editableValues.replace(column.getId(), statusHelper);
      } else {
        StatusHelper helper = new StatusHelper(column.getCustomName(), column.getCustomDescription(),
          column.getSearchStatus().getList().isShow(), value, column.getSearchStatus().getAdvanced().isFixed());
        editableValues.put(column.getId(), helper);
      }

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
        if (editableValues.get(column.getId()) == null) {
          final CheckboxData checkboxData = new CheckboxData();
          checkboxData.setChecked(column.getSearchStatus().getAdvanced().isFixed());
          checkboxData.setDisable(column.getType().equals(ViewerType.dbTypes.BINARY));
          return checkboxData;
          // return column.getSearchStatus().getAdvanced().isFixed();
        } else {
          final CheckboxData checkboxData = new CheckboxData();
          checkboxData.setChecked(editableValues.get(column.getId()).isShowInAdvancedSearch());
          checkboxData.setDisable(column.getType().equals(ViewerType.dbTypes.BINARY));
          return checkboxData;
          // return editableValues.get(column.getId()).isShowInAdvancedSearch();
        }
      }
    };

    checkbox.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        StatusHelper statusHelper = editableValues.get(column.getId());
        statusHelper.setShowInAdvancedSearch(value.isChecked());
        editableValues.replace(column.getId(), statusHelper);
      } else {
        StatusHelper helper = new StatusHelper(column.getCustomName(), column.getCustomDescription(),
          column.getSearchStatus().getList().isShow(), column.getDetailsStatus().isShow(), value.isChecked());
        editableValues.put(column.getId(), helper);
      }

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
            ColumnStatus columnStatus = cellTable.getDataProvider().getList().get(context.getIndex());
            instances.forEach((key, instance) -> {
              if (key.startsWith(database.getUuid())) {
                instance.editableValues.forEach((k, object) -> {
                  if (!k.equals(columnStatus.getId()) && ViewerStringUtils.isBlank(object.getLabel())) {
                    btnSave.setEnabled(false);
                    ObserverManager.getSaveObserver().setEnabled(database.getUuid(), false);
                  }
                });
              }
            });
          }
        }
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
      }
    }) {
      @Override
      public String getValue(ColumnStatus column) {
        if (editableValues.get(column.getId()) == null) {
          StatusHelper helper = new StatusHelper(column.getCustomName(), column.getCustomDescription(),
            column.getSearchStatus().getList().isShow(), column.getDetailsStatus().isShow(),
            column.getSearchStatus().getAdvanced().isFixed());
          editableValues.put(column.getId(), helper);
          return column.getCustomName();
        } else {
          return editableValues.get(column.getId()).getLabel();
        }
      }
    };

    label.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        final StatusHelper statusHelper = editableValues.get(column.getId());
        statusHelper.setLabel(value);
        editableValues.put(column.getId(), statusHelper);
      } else {
        StatusHelper helper = new StatusHelper(value, column.getCustomDescription(),
          column.getSearchStatus().getList().isShow(), column.getDetailsStatus().isShow(),
          column.getSearchStatus().getAdvanced().isFixed());
        editableValues.put(column.getId(), helper);
      }

      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });

    return label;
  }

  private Column<ColumnStatus, String> getDescriptionColumn() {
    Column<ColumnStatus, String> description = new Column<ColumnStatus, String>(new TextAreaInputCell() {}) {
      @Override
      public String getValue(ColumnStatus column) {
        if (editableValues.get(column.getId()) == null) {
          return column.getCustomDescription();
        } else {
          return editableValues.get(column.getId()).getDescription();
        }
      }
    };

    description.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        final StatusHelper statusHelper = editableValues.get(column.getId());
        statusHelper.setDescription(value);
        editableValues.put(column.getId(), statusHelper);
      } else {
        StatusHelper helper = new StatusHelper(column.getCustomName(), value,
          column.getSearchStatus().getList().isShow(), column.getDetailsStatus().isShow(),
          column.getSearchStatus().getAdvanced().isFixed());
        editableValues.put(column.getId(), helper);
      }

      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });
    return description;
  }

  private Column<ColumnStatus, String> getOptionsColumn() {
    Column<ColumnStatus, String> options = new ButtonColumn<ColumnStatus>() {
      @Override
      public void render(Cell.Context context, ColumnStatus object, SafeHtmlBuilder sb) {
        if (object.getType().equals(ViewerType.dbTypes.BINARY) || object.getType().equals(ViewerType.dbTypes.NESTED)) {
          sb.appendHtmlConstant(
            "<div class=\"center-cell\"><button class=\"btn btn-cell-action\" type=\"button\" tabindex=\"-1\"><i class=\"fa fa-cog\"></i></button></div>");
        } else {
          sb.appendHtmlConstant(
            "<div class=\"center-cell\"><button class=\"btn btn-cell-action\" type=\"button\" tabindex=\"-1\" disabled><i class=\"fa fa-cog\"></i></button></div>");
        }
      }

      @Override
      public String getValue(ColumnStatus object) {
        return messages.basicActionOpen();
      }
    };

    options.setFieldUpdater((index, columnStatus, value) -> {
      if (columnStatus.getType().equals(ViewerType.dbTypes.NESTED)) {
        final ColumnOptionsPanel nestedColumnOptionPanel = NestedColumnOptionsPanel.createInstance(columnStatus);
        Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), messages.basicActionSave(),
          messages.basicActionCancel(), nestedColumnOptionPanel, new DefaultAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean value) {
              if (value) {
                collectionStatus.getTableStatusByTableId(tableId).getColumnById(columnStatus.getId())
                  .updateSearchListTemplate(nestedColumnOptionPanel.getSearchTemplate());
                collectionStatus.getTableStatusByTableId(tableId).getColumnById(columnStatus.getId())
                  .updateDetailsTemplate(nestedColumnOptionPanel.getDetailsTemplate());
                collectionStatus.getTableStatusByTableId(tableId).getColumnById(columnStatus.getId())
                  .updateExportTemplate(nestedColumnOptionPanel.getExportTemplate());
                collectionStatus.getTableStatusByTableId(tableId).getColumnById(columnStatus.getId())
                  .updateNestedColumnsQuantityList(
                    ((NestedColumnOptionsPanel) nestedColumnOptionPanel).getQuantityInList());
                saveChanges();
              }
            }
          });
      } else if (columnStatus.getType().equals(ViewerType.dbTypes.BINARY)) {
        ColumnOptionsPanel binaryColumnOptionPanel = BinaryColumnOptionsPanel.createInstance(
          collectionStatus.getTableStatusByTableId(tableId),
          collectionStatus.getTableStatusByTableId(tableId).getColumnById(columnStatus.getId()));

        Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), messages.basicActionSave(),
          messages.basicActionCancel(), binaryColumnOptionPanel, new DefaultAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean value) {
              if (value) {
                collectionStatus.getTableStatusByTableId(tableId).getColumnById(columnStatus.getId())
                  .updateExportTemplate(binaryColumnOptionPanel.getExportTemplate());
                collectionStatus.getTableStatusByTableId(tableId).getColumnById(columnStatus.getId())
                  .updateSearchListTemplate(binaryColumnOptionPanel.getSearchTemplate());
                collectionStatus.getTableStatusByTableId(tableId).getColumnById(columnStatus.getId())
                  .updateDetailsTemplate(binaryColumnOptionPanel.getDetailsTemplate());
                collectionStatus.getTableStatusByTableId(tableId).getColumnById(columnStatus.getId())
                  .setApplicationType(((BinaryColumnOptionsPanel) binaryColumnOptionPanel).getApplicationType());
                saveChanges();
              }
            }
          });
      }
    });
    return options;
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

  private boolean validateUniqueInputs() {
    for (Map.Entry<String, ColumnsManagementPanel> entry : instances.entrySet()) {
      if (entry.getKey().startsWith(database.getUuid()) && (!validateUniqueInput(entry.getValue().editableValues))) {
        return false;
      }
    }
    return true;
  }

  private boolean validateUniqueInput(Map<String, StatusHelper> editableValues) {
    Set<String> uniques = new HashSet<>();

    for (StatusHelper value : editableValues.values()) {
      if (!uniques.add(value.getLabel())) {
        return false;
      }
    }
    return true;
  }

  private boolean validateCheckboxes() {
    for (Map.Entry<String, ColumnsManagementPanel> entry : instances.entrySet()) {
      if (entry.getKey().startsWith(database.getUuid())
        && (!validateCheckbox(entry.getValue().editableValues, collectionStatus.getTableStatusByTableId(tableId)))) {
        return false;
      }
    }
    return true;
  }

  private boolean validateCheckbox(Map<String, StatusHelper> editableValues, TableStatus tableStatus) {
    int countDetails = 0;
    int countTable = 0;

    if (editableValues.isEmpty()) {
      return true;
    }

    for (StatusHelper helper : editableValues.values()) {
      if (!helper.isShowInDetails()) {
        countDetails++;
      }
      if (!helper.isShowInTable()) {
        countTable++;
      }
    }

    int remainingDetails = tableStatus.getColumns().size() - countDetails;
    int remainingTable = tableStatus.getColumns().size() - countTable;

    return remainingDetails > 0 && remainingTable > 0;
  }

  private void saveChanges() {
    CollectionService.Util.call((Boolean result) -> {
      ObserverManager.getCollectionObserver().setCollectionStatus(collectionStatus);
      sidebar.reset(database, collectionStatus);
      this.changes = false;
      Toast.showInfo(messages.columnManagementPageTitle(), messages.columnManagementPageToastDescription());
    }).updateCollectionConfiguration(database.getUuid(), database.getUuid(), collectionStatus);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forColumnsManagement(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  @Override
  public void updateCollection(CollectionStatus collectionStatus) {
    sidebar.reset(database, collectionStatus);
    this.collectionStatus = collectionStatus;
  }

  @Override
  public void update(String databaseUUID, boolean enabled) {
    if (database.getUuid().equals(databaseUUID)) {
      btnSave.setEnabled(enabled);
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if (!collectionStatus.getTableStatusByTableId(tableId).isShow()) {
      HistoryManager.gotoAdvancedConfiguration(database.getUuid());
    }
  }
}
