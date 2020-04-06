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
import com.databasepreservation.common.client.common.lists.cells.RequiredEditableCell;
import com.databasepreservation.common.client.common.lists.cells.TextAreaInputCell;
import com.databasepreservation.common.client.common.lists.columns.ButtonColumn;
import com.databasepreservation.common.client.common.lists.widgets.BasicTablePanel;
import com.databasepreservation.common.client.common.sidebar.Sidebar;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.BinaryColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.NestedColumnOptionsPanel;
import com.databasepreservation.common.client.configuration.observer.ICollectionStatusObserver;
import com.databasepreservation.common.client.configuration.observer.ISaveButtonObserver;
import com.databasepreservation.common.client.models.configuration.collection.ViewerCollectionConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerColumnConfiguration;
import com.databasepreservation.common.client.models.configuration.collection.ViewerTableConfiguration;
import com.databasepreservation.common.client.models.configuration.helpers.ConfigurationHelper;
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
  private ViewerCollectionConfiguration viewerCollectionConfiguration;
  private ViewerDatabase database;
  private Sidebar sidebar;
  private String tableId;
  private Button btnSave = new Button();
  private BasicTablePanel<ViewerColumnConfiguration> cellTable;
  private boolean changes = false;
  private Map<String, ConfigurationHelper> editableValues = new HashMap<>();

  public static ColumnsManagementPanel getInstance(ViewerCollectionConfiguration status, ViewerDatabase database, String tableUUID,
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

  private ColumnsManagementPanel(ViewerDatabase database, ViewerCollectionConfiguration viewerCollectionConfiguration, String tableId,
                                 Sidebar sidebar) {
    initWidget(binder.createAndBindUi(this));
    ObserverManager.getCollectionObserver().addObserver(this);
    ObserverManager.getSaveObserver().addObserver(this);
    this.database = database;
    this.viewerCollectionConfiguration = viewerCollectionConfiguration;
    this.tableId = tableId;
    this.sidebar = sidebar;

    init();
  }

  private void init() {
    configureHeader();
    final ViewerTableConfiguration table = viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId);
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
      viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).getCustomName(), "h1"), 0);
    mainHeader.setTitle(viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).getCustomDescription());

    MetadataField instance = MetadataField.createInstance(messages.columnManagementPageDescription());
    instance.setCSS("table-row-description", "font-size-description");

    content.add(instance);

    btnGotoTable.setText(messages.dataTransformationBtnBrowseTable());
    btnGotoTable.addClickHandler(e -> HistoryManager.gotoTable(database.getUuid(), tableId));
  }

  private List<ViewerColumnConfiguration> saveChanges(BasicTablePanel<ViewerColumnConfiguration> cellTable,
                                                      Map<String, ConfigurationHelper> editableValues) {
    List<ViewerColumnConfiguration> statuses = new ArrayList<>();
    for (ViewerColumnConfiguration column : cellTable.getDataProvider().getList()) {
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
              sidebar.reset(database, viewerCollectionConfiguration);
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
              object.viewerCollectionConfiguration.getViewerTableConfigurationByTableId(object.tableId)
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

  private BasicTablePanel<ViewerColumnConfiguration> populateTable(final ViewerTableConfiguration viewerTableConfiguration) {
    Collections.sort(viewerTableConfiguration.getColumns());
    BasicTablePanel<ViewerColumnConfiguration> tablePanel = new BasicTablePanel<>(new FlowPanel(), SafeHtmlUtils.EMPTY_SAFE_HTML,
      GWT.create(ConfigurationCellTableResources.class), viewerTableConfiguration.getColumns().iterator(),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderOrder(), 6, getOrderColumn()),
      new BasicTablePanel.ColumnInfo<>(messages.basicTableHeaderTableOrColumn("name"), 0,
        new Column<ViewerColumnConfiguration, SafeHtml>(new SafeHtmlCell()) {
          @Override
          public SafeHtml getValue(ViewerColumnConfiguration column) {
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
      final ViewerColumnConfiguration selected = tablePanel.getSelectionModel().getSelectedObject();
      if (selected != null) {
        tablePanel.getSelectionModel().clear();
      }
    });

    return tablePanel;
  }

  private Column<ViewerColumnConfiguration, ViewerColumnConfiguration> getOrderColumn() {
    List<HasCell<ViewerColumnConfiguration, ?>> cells = new ArrayList<>();

    cells
      .add(new ActionsCell<>(messages.columnManagementPageTextForArrowUp(), FontAwesomeIconManager.ARROW_UP, object -> {
        List<ViewerColumnConfiguration> list = cellTable.getDataProvider().getList();
        if (object.getOrder() != 1) {
          updateColumnOrder(list, object.getOrder() - 2, object.getOrder() - 1);
          cellTable.getDataProvider().setList(list);
          cellTable.getDataProvider().refresh();
        }
      }));

    cells.add(
      new ActionsCell<>(messages.columnManagementPageTextForArrowDown(), FontAwesomeIconManager.ARROW_DOWN, object -> {
        List<ViewerColumnConfiguration> list = cellTable.getDataProvider().getList();
        if (object.getOrder() != viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).getColumns().size()) {
          updateColumnOrder(list, object.getOrder(), object.getOrder() - 1);
          cellTable.getDataProvider().setList(list);
          cellTable.getDataProvider().refresh();
        }
      }));

    CompositeCell<ViewerColumnConfiguration> compositeCell = new CompositeCell<>(cells);
    return new Column<ViewerColumnConfiguration, ViewerColumnConfiguration>(compositeCell) {
      @Override
      public ViewerColumnConfiguration getValue(ViewerColumnConfiguration viewerColumnConfiguration) {
        return viewerColumnConfiguration;
      }
    };
  }

  private Column<ViewerColumnConfiguration, Boolean> getTableCheckboxColumn() {
    Column<ViewerColumnConfiguration, Boolean> checkbox = new Column<ViewerColumnConfiguration, Boolean>(new CheckboxCell(true, true)) {
      @Override
      public Boolean getValue(ViewerColumnConfiguration column) {
        if (editableValues.get(column.getId()) == null) {
          ConfigurationHelper helper = new ConfigurationHelper(column.getCustomName(), column.getCustomDescription(),
            column.getViewerSearchConfiguration().getList().isShow(), column.getViewerDetailsConfiguration().isShow(),
            column.getViewerSearchConfiguration().getAdvanced().isFixed());
          editableValues.put(column.getId(), helper);
          return column.getViewerSearchConfiguration().getList().isShow();
        } else {
          return editableValues.get(column.getId()).isShowInTable();
        }
      }
    };

    checkbox.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        ConfigurationHelper configurationHelper = editableValues.get(column.getId());
        configurationHelper.setShowInTable(value);
        editableValues.replace(column.getId(), configurationHelper);
      } else {
        ConfigurationHelper helper = new ConfigurationHelper(column.getCustomName(), column.getCustomDescription(), value,
          column.getViewerDetailsConfiguration().isShow(), column.getViewerSearchConfiguration().getAdvanced().isFixed());
        editableValues.put(column.getId(), helper);
      }

      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });

    return checkbox;
  }

  private Column<ViewerColumnConfiguration, Boolean> getDetailsCheckboxColumn() {
    Column<ViewerColumnConfiguration, Boolean> checkbox = new Column<ViewerColumnConfiguration, Boolean>(new CheckboxCell(true, true)) {
      @Override
      public Boolean getValue(ViewerColumnConfiguration column) {
        if (editableValues.get(column.getId()) == null) {
          ConfigurationHelper helper = new ConfigurationHelper(column.getCustomName(), column.getCustomDescription(),
            column.getViewerSearchConfiguration().getList().isShow(), column.getViewerDetailsConfiguration().isShow(),
            column.getViewerSearchConfiguration().getAdvanced().isFixed());
          editableValues.put(column.getId(), helper);
          return column.getViewerDetailsConfiguration().isShow();
        } else {
          return editableValues.get(column.getId()).isShowInDetails();
        }
      }
    };

    checkbox.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        ConfigurationHelper configurationHelper = editableValues.get(column.getId());
        configurationHelper.setShowInDetails(value);
        editableValues.replace(column.getId(), configurationHelper);
      } else {
        ConfigurationHelper helper = new ConfigurationHelper(column.getCustomName(), column.getCustomDescription(),
          column.getViewerSearchConfiguration().getList().isShow(), value, column.getViewerSearchConfiguration().getAdvanced().isFixed());
        editableValues.put(column.getId(), helper);
      }

      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });

    return checkbox;
  }

  private Column<ViewerColumnConfiguration, Boolean> getAdvancedSearchCheckboxColumn() {
    Column<ViewerColumnConfiguration, Boolean> checkbox = new Column<ViewerColumnConfiguration, Boolean>(new CheckboxCell(true, true)) {
      @Override
      public Boolean getValue(ViewerColumnConfiguration column) {
        if (editableValues.get(column.getId()) == null) {
          return column.getViewerSearchConfiguration().getAdvanced().isFixed();
        } else {
          return editableValues.get(column.getId()).isShowInAdvancedSearch();
        }
      }
    };

    checkbox.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        ConfigurationHelper configurationHelper = editableValues.get(column.getId());
        configurationHelper.setShowInAdvancedSearch(value);
        editableValues.replace(column.getId(), configurationHelper);
      } else {
        ConfigurationHelper helper = new ConfigurationHelper(column.getCustomName(), column.getCustomDescription(),
          column.getViewerSearchConfiguration().getList().isShow(), column.getViewerDetailsConfiguration().isShow(), value);
        editableValues.put(column.getId(), helper);
      }

      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });

    return checkbox;
  }

  private Column<ViewerColumnConfiguration, String> getLabelColumn() {
    Column<ViewerColumnConfiguration, String> label = new Column<ViewerColumnConfiguration, String>(new RequiredEditableCell("") {
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
            ViewerColumnConfiguration viewerColumnConfiguration = cellTable.getDataProvider().getList().get(context.getIndex());
            instances.forEach((key, instance) -> {
              if (key.startsWith(database.getUuid())) {
                instance.editableValues.forEach((k, object) -> {
                  if (!k.equals(viewerColumnConfiguration.getId()) && ViewerStringUtils.isBlank(object.getLabel())) {
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
      public String getValue(ViewerColumnConfiguration column) {
        if (editableValues.get(column.getId()) == null) {
          ConfigurationHelper helper = new ConfigurationHelper(column.getCustomName(), column.getCustomDescription(),
            column.getViewerSearchConfiguration().getList().isShow(), column.getViewerDetailsConfiguration().isShow(),
            column.getViewerSearchConfiguration().getAdvanced().isFixed());
          editableValues.put(column.getId(), helper);
          return column.getCustomName();
        } else {
          return editableValues.get(column.getId()).getLabel();
        }
      }
    };

    label.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        final ConfigurationHelper configurationHelper = editableValues.get(column.getId());
        configurationHelper.setLabel(value);
        editableValues.put(column.getId(), configurationHelper);
      } else {
        ConfigurationHelper helper = new ConfigurationHelper(value, column.getCustomDescription(),
          column.getViewerSearchConfiguration().getList().isShow(), column.getViewerDetailsConfiguration().isShow(),
          column.getViewerSearchConfiguration().getAdvanced().isFixed());
        editableValues.put(column.getId(), helper);
      }

      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });

    return label;
  }

  private Column<ViewerColumnConfiguration, String> getDescriptionColumn() {
    Column<ViewerColumnConfiguration, String> description = new Column<ViewerColumnConfiguration, String>(new TextAreaInputCell() {}) {
      @Override
      public String getValue(ViewerColumnConfiguration column) {
        if (editableValues.get(column.getId()) == null) {
          return column.getCustomDescription();
        } else {
          return editableValues.get(column.getId()).getDescription();
        }
      }
    };

    description.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        final ConfigurationHelper configurationHelper = editableValues.get(column.getId());
        configurationHelper.setDescription(value);
        editableValues.put(column.getId(), configurationHelper);
      } else {
        ConfigurationHelper helper = new ConfigurationHelper(column.getCustomName(), value,
          column.getViewerSearchConfiguration().getList().isShow(), column.getViewerDetailsConfiguration().isShow(),
          column.getViewerSearchConfiguration().getAdvanced().isFixed());
        editableValues.put(column.getId(), helper);
      }

      changes = true;
      sidebar.updateSidebarItem(tableId, true);
    });
    return description;
  }

  private Column<ViewerColumnConfiguration, String> getOptionsColumn() {
    Column<ViewerColumnConfiguration, String> options = new ButtonColumn<ViewerColumnConfiguration>() {
      @Override
      public void render(Cell.Context context, ViewerColumnConfiguration object, SafeHtmlBuilder sb) {
        if (object.getType().equals(ViewerType.dbTypes.BINARY) || object.getType().equals(ViewerType.dbTypes.NESTED)) {
          sb.appendHtmlConstant(
            "<div class=\"center-cell\"><button class=\"btn btn-cell-action\" type=\"button\" tabindex=\"-1\"><i class=\"fa fa-cog\"></i></button></div>");
        } else {
          sb.appendHtmlConstant(
            "<div class=\"center-cell\"><button class=\"btn btn-cell-action\" type=\"button\" tabindex=\"-1\" disabled><i class=\"fa fa-cog\"></i></button></div>");
        }
      }

      @Override
      public String getValue(ViewerColumnConfiguration object) {
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
                viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).getColumnById(columnStatus.getId())
                  .updateSearchListTemplate(nestedColumnOptionPanel.getSearchTemplate());
                viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).getColumnById(columnStatus.getId())
                  .updateDetailsTemplate(nestedColumnOptionPanel.getDetailsTemplate());
                viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).getColumnById(columnStatus.getId())
                  .updateExportTemplate(nestedColumnOptionPanel.getExportTemplate());
                viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).getColumnById(columnStatus.getId())
                  .updateNestedColumnsQuantityList(
                    ((NestedColumnOptionsPanel) nestedColumnOptionPanel).getQuantityInList());
                saveChanges();
              }
            }
          });
      } else if (columnStatus.getType().equals(ViewerType.dbTypes.BINARY)) {
        ColumnOptionsPanel binaryColumnOptionPanel = BinaryColumnOptionsPanel.createInstance(
          viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId),
          viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).getColumnById(columnStatus.getId()));

        Dialogs.showDialogColumnConfiguration(messages.basicTableHeaderOptions(), messages.basicActionSave(),
          messages.basicActionCancel(), binaryColumnOptionPanel, new DefaultAsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean value) {
              if (value) {
                viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).getColumnById(columnStatus.getId())
                  .updateExportTemplate(binaryColumnOptionPanel.getExportTemplate());
                viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).getColumnById(columnStatus.getId())
                    .updateSearchListTemplate(binaryColumnOptionPanel.getSearchTemplate());
                viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).getColumnById(columnStatus.getId())
                    .updateDetailsTemplate(binaryColumnOptionPanel.getDetailsTemplate());
                viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).getColumnById(columnStatus.getId())
                  .setApplicationType(((BinaryColumnOptionsPanel) binaryColumnOptionPanel).getApplicationType());
                saveChanges();
              }
            }
          });
      }
    });
    return options;
  }

  private void updateColumnOrder(List<ViewerColumnConfiguration> list, int relativeToClickIndex, int clickedIndex) {
    ViewerColumnConfiguration relative = list.get(relativeToClickIndex);
    ViewerColumnConfiguration clicked = list.get(clickedIndex);

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

  private boolean validateUniqueInput(Map<String, ConfigurationHelper> editableValues) {
    Set<String> uniques = new HashSet<>();

    for (ConfigurationHelper value : editableValues.values()) {
      if (!uniques.add(value.getLabel())) {
        return false;
      }
    }
    return true;
  }

  private boolean validateCheckboxes() {
    for (Map.Entry<String, ColumnsManagementPanel> entry : instances.entrySet()) {
      if (entry.getKey().startsWith(database.getUuid())
        && (!validateCheckbox(entry.getValue().editableValues, viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId)))) {
        return false;
      }
    }
    return true;
  }

  private boolean validateCheckbox(Map<String, ConfigurationHelper> editableValues, ViewerTableConfiguration viewerTableConfiguration) {
    int countDetails = 0;
    int countTable = 0;

    if (editableValues.isEmpty()) {
      return true;
    }

    for (ConfigurationHelper helper : editableValues.values()) {
      if (!helper.isShowInDetails()) {
        countDetails++;
      }
      if (!helper.isShowInTable()) {
        countTable++;
      }
    }

    int remainingDetails = viewerTableConfiguration.getColumns().size() - countDetails;
    int remainingTable = viewerTableConfiguration.getColumns().size() - countTable;

    return remainingDetails > 0 && remainingTable > 0;
  }

  private void saveChanges() {
    CollectionService.Util.call((Boolean result) -> {
      ObserverManager.getCollectionObserver().setCollectionStatus(viewerCollectionConfiguration);
      sidebar.reset(database, viewerCollectionConfiguration);
      this.changes = false;
      Toast.showInfo(messages.columnManagementPageTitle(), messages.columnManagementPageToastDescription());
    }).updateCollectionConfiguration(database.getUuid(), database.getUuid(), viewerCollectionConfiguration);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forColumnsManagement(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  @Override
  public void updateCollection(ViewerCollectionConfiguration viewerCollectionConfiguration) {
    sidebar.reset(database, viewerCollectionConfiguration);
    this.viewerCollectionConfiguration = viewerCollectionConfiguration;
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
    if (!viewerCollectionConfiguration.getViewerTableConfigurationByTableId(tableId).isShow()) {
      HistoryManager.gotoAdvancedConfiguration(database.getUuid());
    }
  }
}
