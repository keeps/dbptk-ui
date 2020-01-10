package com.databasepreservation.common.client.common.visualization.browse.configuration.columns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.cells.ActionsCell;
import com.databasepreservation.common.client.common.lists.cells.EditableCell;
import com.databasepreservation.common.client.common.lists.cells.RequiredEditableCell;
import com.databasepreservation.common.client.common.lists.cells.TextAreaInputCell;
import com.databasepreservation.common.client.common.lists.widgets.BasicTablePanel;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.sidebar.ColumnsManagementSidebar;
import com.databasepreservation.common.client.common.sidebar.Sidebar;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.configuration.observer.CollectionStatusObserver;
import com.databasepreservation.common.client.configuration.observer.ISaveButtonObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.helpers.StatusHelper;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.ConfigurationCellTableResources;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
public class ColumnsManagementPanel extends RightPanel implements CollectionStatusObserver, ISaveButtonObserver {

  private ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  SimplePanel mainHeader;

  @UiField
  FlowPanel content;

  interface TableManagementPanelUiBinder extends UiBinder<Widget, ColumnsManagementPanel> {
  }

  private static TableManagementPanelUiBinder binder = GWT.create(TableManagementPanelUiBinder.class);

  private static Map<String, ColumnsManagementPanel> instances = new HashMap<>();
  private CollectionStatus collectionStatus;
  private ViewerDatabase database;
  private Sidebar sidebar;
  private String tableUUID;
  private Button btnSave = new Button();
  private BasicTablePanel<ColumnStatus> cellTable;
  private Map<String, StatusHelper> editableValues = new HashMap<>();

  public static ColumnsManagementPanel getInstance(CollectionStatus status, ViewerDatabase database, String tableUUID,
    Sidebar sidebar) {
    final String value;
    if (tableUUID == null) {
      value = status.getTables().get(0).getUuid();
    } else {
      value = tableUUID;
    }
    return instances.computeIfAbsent(database.getUuid() + value,
      k -> new ColumnsManagementPanel(database, status, value, sidebar));
  }

  private ColumnsManagementPanel(ViewerDatabase database, CollectionStatus collectionStatus, String tableUUID,
    Sidebar sidebar) {
    initWidget(binder.createAndBindUi(this));
    ObserverManager.getCollectionObserver().addObserver(this);
    ObserverManager.getSaveObserver().addObserver(this);
    this.database = database;
    this.collectionStatus = collectionStatus;
    this.tableUUID = tableUUID;
    this.sidebar = sidebar;

    init();
  }

  private void init() {
    configureHeader();
    final TableStatus table = collectionStatus.getTableStatus(tableUUID);
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

    btnCancel.addClickHandler(clickEvent -> {
      instances.entrySet().removeIf(e -> e.getKey().startsWith(database.getUuid()));
      sidebar.reset(database, collectionStatus);
      HistoryManager.gotoAdvancedConfiguration(database.getUuid());
    });

    btnSave.addClickHandler(clickEvent -> {
      if (validateCheckboxes()) {
        instances.forEach((key, object) -> {
          if (key.startsWith(database.getUuid())) {
            object.collectionStatus.getTableStatus(object.tableUUID)
              .setColumns(saveChanges(object.cellTable, object.tableUUID, object.editableValues));
          }
        });

        DatabaseService.Util.call((Boolean result) -> {
          ObserverManager.getCollectionObserver().setCollectionStatus(collectionStatus);
          sidebar.reset(database, collectionStatus);
          Toast.showInfo(messages.columnManagementPageTitle(), messages.columnManagementPageToastDescription());
        }).updateCollectionConfiguration(database.getUuid(), database.getUuid(), collectionStatus);
      } else {
        Dialogs.showErrors(messages.columnManagementPageTitle(), messages.columnManagementPageDialogErrorDescription(),
          messages.basicActionClose());
      }
    });

    content.add(CommonClientUtils.wrapOnDiv("navigation-panel-buttons",
      CommonClientUtils.wrapOnDiv("btn-item", btnSave), CommonClientUtils.wrapOnDiv("btn-item", btnCancel)));
  }

  private void configureHeader() {
    mainHeader.setWidget(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE),
      collectionStatus.getTableStatus(tableUUID).getCustomName(), "h1"));

    MetadataField instance = MetadataField
      .createInstance(collectionStatus.getTableStatus(tableUUID).getCustomDescription());
    instance.setCSS("table-row-description", "font-size-description");

    content.add(instance);
  }

  private List<ColumnStatus> saveChanges(BasicTablePanel<ColumnStatus> cellTable, String tableUUID,
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

  private BasicTablePanel<ColumnStatus> populateTable(final TableStatus tableStatus) {
    Collections.sort(tableStatus.getColumns());
    BasicTablePanel<ColumnStatus> tablePanel = new BasicTablePanel<ColumnStatus>(new FlowPanel(),
      SafeHtmlUtils.EMPTY_SAFE_HTML, GWT.create(ConfigurationCellTableResources.class),
      tableStatus.getColumns().iterator(),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderOrder(), 6, getOrderColumn()),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderTableOrColumn("name"), 10,
        new TextColumn<ColumnStatus>() {
          @Override
          public String getValue(ColumnStatus column) {
            return column.getName();
          }
        }),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderLabel(), 15, getLabelColumn()),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderDescription(), 0, getDescriptionColumn()),
      new BasicTablePanel.ColumnInfo<>(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
        .getTagWithStyleName(FontAwesomeIconManager.TABLE, messages.columnManagementPageTooltipForTable(), "fa-fw")),
        false, 3, getTableCheckboxColumn()),
      new BasicTablePanel.ColumnInfo<>(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
        .getTagWithStyleName(FontAwesomeIconManager.LIST, messages.columnManagementPageTooltipForDetails(), "fa-fw")),
        false, 3, getDetailsCheckboxColumn()),
      new BasicTablePanel.ColumnInfo<>(
        SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTagWithStyleName(FontAwesomeIconManager.SEARCH_PLUS,
          messages.columnManagementPageTooltipForAdvancedSearch(), "fa-fw")),
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
        if (object.getOrder() != collectionStatus.getTableStatus(tableUUID).getColumns().size()) {
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

      ColumnsManagementSidebar.getInstance(database.getUuid()).updateSidebarItem(tableUUID,
        !value.equals(column.getSearchStatus().getList().isShow()));
    });

    return checkbox;
  }

  private Column<ColumnStatus, Boolean> getDetailsCheckboxColumn() {
    Column<ColumnStatus, Boolean> checkbox = new Column<ColumnStatus, Boolean>(new CheckboxCell(true, true)) {
      @Override
      public Boolean getValue(ColumnStatus column) {
        if (editableValues.get(column.getId()) == null) {
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

      ColumnsManagementSidebar.getInstance(database.getUuid()).updateSidebarItem(tableUUID,
        !value.equals(column.getDetailsStatus().isShow()));
    });

    return checkbox;
  }

  private Column<ColumnStatus, Boolean> getAdvancedSearchCheckboxColumn() {
    Column<ColumnStatus, Boolean> checkbox = new Column<ColumnStatus, Boolean>(new CheckboxCell(true, true)) {
      @Override
      public Boolean getValue(ColumnStatus column) {
        if (editableValues.get(column.getId()) == null) {
          return column.getSearchStatus().getAdvanced().isFixed();
        } else {
          return editableValues.get(column.getId()).isShowInAdvancedSearch();
        }
      }
    };

    checkbox.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        StatusHelper statusHelper = editableValues.get(column.getId());
        statusHelper.setShowInAdvancedSearch(value);
        editableValues.replace(column.getId(), statusHelper);
      } else {
        StatusHelper helper = new StatusHelper(column.getCustomName(), column.getCustomDescription(),
          column.getSearchStatus().getList().isShow(), column.getDetailsStatus().isShow(), value);
        editableValues.put(column.getId(), helper);
      }

      sidebar.updateSidebarItem(tableUUID, !value.equals(column.getSearchStatus().getAdvanced().isFixed()));
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

      sidebar.updateSidebarItem(tableUUID, true);
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

      sidebar.updateSidebarItem(tableUUID, true);
    });
    return description;
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
  }

  private boolean validateCheckboxes() {
    for (Map.Entry<String, ColumnsManagementPanel> entry : instances.entrySet()) {
      if (entry.getKey().startsWith(database.getUuid())) {
        if (!validateCheckbox(entry.getValue().editableValues)) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean validateCheckbox(Map<String, StatusHelper> editableValues) {
    int countDetails = 0;
    int countTable = 0;

    if (editableValues.isEmpty()) {
      return true;
    }

    for (StatusHelper helper : editableValues.values()) {
      if (helper.isShowInDetails())
        countDetails++;
      if (helper.isShowInTable())
        countTable++;
    }

    return countDetails != 0 && countTable != 0;
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forColumnsManagement(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  @Override
  public void updateCollection(CollectionStatus collectionStatus) {
    this.collectionStatus = collectionStatus;
  }

  @Override
  public void update(String databaseUUID, boolean enabled) {
    if (database.getUuid().equals(databaseUUID)) {
      btnSave.setEnabled(enabled);
    }
  }
}
