package com.databasepreservation.common.client.common.visualization.browse.configuration.columns;

import java.util.ArrayList;
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
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.configuration.observer.CollectionObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.helpers.ColumnStatusHelper;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
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
  private String tableUUID;
  private CollectionStatus collectionStatus;
  private MultipleSelectionTablePanel<ColumnStatus> multipleSelectionTablePanel;
  private Map<String, Boolean> initialLoading = new HashMap<>();
  private Map<String, ColumnStatusHelper> editableValues = new HashMap<>();

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
    this.tableUUID = tableUUID;
    if (tableUUID != null) {
      init();
    }
  }

  private void init() {
    configureHeader();
    final TableStatus table = collectionStatus.getTableStatus(tableUUID);

    multipleSelectionTablePanel = createCellTable();
    populateTable(multipleSelectionTablePanel, table);
    content.add(multipleSelectionTablePanel);

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
      List<ColumnStatus> statuses = new ArrayList<>();
      for (ColumnStatus column : collectionStatus.getTableStatus(tableUUID).getColumns()) {
        column.updateHidingAttribute(!multipleSelectionTablePanel.getSelectionModel().isSelected(column));
        if (editableValues.get(column.getId()) != null) {
          column.setCustomDescription(editableValues.get(column.getId()).getDescription());
          column.setCustomName(editableValues.get(column.getId()).getLabel());
        }
        statuses.add(column);
        GWT.log(column.toString());
      }

      collectionStatus.getTableStatus(tableUUID).setColumns(statuses);

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
      collectionStatus.getTableStatus(tableUUID).getCustomName(), "h1"));

    MetadataField instance = MetadataField
      .createInstance(collectionStatus.getTableStatus(tableUUID).getCustomDescription());
    instance.setCSS("table-row-description", "font-size-description");

    content.add(instance);
  }

  private MultipleSelectionTablePanel<ColumnStatus> createCellTable() {
    return new MultipleSelectionTablePanel<>();
  }

  private void populateTable(MultipleSelectionTablePanel<ColumnStatus> selectionTablePanel,
    final TableStatus tableStatus) {
    selectionTablePanel.createTable(new FlowPanel(), Arrays.asList(1, 2), tableStatus.getColumns().iterator(),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.basicTableHeaderShow(), 4,
        new Column<ColumnStatus, Boolean>(new CheckboxCell(true, true)) {
          @Override
          public Boolean getValue(ColumnStatus column) {
            if (initialLoading.get(column.getId()) == null) {
              initialLoading.put(column.getId(), false);
              selectionTablePanel.getSelectionModel().setSelected(column,
                collectionStatus.showColumn(tableUUID, column.getId()));
            }

            return selectionTablePanel.getSelectionModel().isSelected(column);
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.basicTableHeaderTableOrColumn("original"),
        10, new TextColumn<ColumnStatus>() {
          @Override
          public String getValue(ColumnStatus column) {
            return column.getName();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.basicTableHeaderLabel(), 15,
        getLabelColumn()),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.basicTableHeaderDescription(), 0,
        getDescriptionColumn()));
  }

  private Column<ColumnStatus, String> getLabelColumn() {
    Column<ColumnStatus, String> label = new Column<ColumnStatus, String>(new EditableCell() {}) {
      @Override
      public String getValue(ColumnStatus column) {
        return column.getCustomName();
      }
    };

    label.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        final ColumnStatusHelper columnStatusHelper = editableValues.get(column.getId());
        columnStatusHelper.setLabel(value);
        editableValues.put(column.getId(), columnStatusHelper);
      } else {
        ColumnStatusHelper helper = new ColumnStatusHelper(value, column.getCustomDescription());
        editableValues.put(column.getId(), helper);
      }
    });
    return label;
  }

  private Column<ColumnStatus, String> getDescriptionColumn() {
    Column<ColumnStatus, String> description = new Column<ColumnStatus, String>(new EditableCell() {}) {
      @Override
      public String getValue(ColumnStatus column) {
        return column.getCustomDescription();
      }
    };

    description.setFieldUpdater((index, column, value) -> {
      if (editableValues.get(column.getId()) != null) {
        final ColumnStatusHelper columnStatusHelper = editableValues.get(column.getId());
        columnStatusHelper.setDescription(value);
        editableValues.put(column.getId(), columnStatusHelper);
      } else {
        ColumnStatusHelper helper = new ColumnStatusHelper(column.getCustomName(), value);
        editableValues.put(column.getId(), helper);
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