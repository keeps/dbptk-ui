package com.databasepreservation.common.client.common.visualization.browse.configuration.table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ValidatableOptionsPanel;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualTableStatus;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualTableOptionsPanel extends ColumnOptionsPanel implements ValidatableOptionsPanel {

  interface VirtualTableOptionsPanelUiBinder extends UiBinder<Widget, VirtualTableOptionsPanel> {
  }

  private static VirtualTableOptionsPanelUiBinder binder = GWT.create(VirtualTableOptionsPanelUiBinder.class);

  private final ViewerDatabase database;
  private final CollectionStatus collectionStatus;
  private List<ViewerColumn> columnsToInclude = new ArrayList<>();

  @UiField
  ClientMessages messages;
  @UiField
  TextBox virtualTableName, virtualTableDescription;
  @UiField
  Label errorVirtualTableName, errorSourceTable;
  @UiField
  ListBox sourceTableListBox;
  @UiField
  FlowPanel templateSourceColumns;

  public static VirtualTableOptionsPanel createInstance(ViewerDatabase database, CollectionStatus collectionStatus) {
    return new VirtualTableOptionsPanel(database, collectionStatus);
  }

  private VirtualTableOptionsPanel(ViewerDatabase database, CollectionStatus collectionStatus) {
    initWidget(binder.createAndBindUi(this));
    this.collectionStatus = collectionStatus;
    this.database = database;

    setSourceTablesDropdown();
    bindEvents();
  }

  private void bindEvents() {
    sourceTableListBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        onSourceTableChanged();
      }
    });
  }

  @Override
  public boolean validate() {
    boolean isValid = true;

    clearError(virtualTableName, errorVirtualTableName);
    clearError(sourceTableListBox, errorSourceTable);

    String nameValue = virtualTableName.getText();

    if (ViewerStringUtils.isBlank(nameValue)) {
      showError(virtualTableName, errorVirtualTableName,
        messages.tableManagementLabelForVirtualTableName() + " is required.");
      isValid = false;
    }

    if (sourceTableListBox.getSelectedIndex() == 0) {
      showError(sourceTableListBox, errorSourceTable, messages.tableManagementLabelForSourceTable() + " is required.");
      isValid = false;
    }

    return isValid;
  }

  private void setSourceTablesDropdown() {
    sourceTableListBox.clear();
    sourceTableListBox.addItem("");
    collectionStatus.getTables().forEach(table -> sourceTableListBox.addItem(table.getName(), table.getUuid()));
  }

  private void onSourceTableChanged() {
    String selectedTableId = sourceTableListBox.getSelectedValue();
    if (selectedTableId.isEmpty()) {
      resetVirtualFields();
    } else {
      resetVirtualFields();
      templateSourceColumns.add(populateColumnsOptions(selectedTableId));
      templateSourceColumns.getElement().getStyle().setProperty("maxHeight", "200px");
    }
  }

  @NotNull
  private MultipleSelectionTablePanel<ViewerColumn> populateColumnsOptions(String selectedTableId) {
    ViewerTable viewerTable = database.getMetadata().getTable(selectedTableId);
    MultipleSelectionTablePanel<ViewerColumn> selectionColumnPanel = new MultipleSelectionTablePanel<>();
    Header<Boolean> selectAllHeader = getSelectAllHeader(viewerTable, selectionColumnPanel);

    selectionColumnPanel.createTable(new FlowPanel(), new ArrayList<>(), viewerTable.getColumns().iterator(),
      // checkbox
      new MultipleSelectionTablePanel.ColumnInfo<>(selectAllHeader, 1,
        getCheckboxColumn(viewerTable, selectionColumnPanel)),
      // column name
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.basicTableHeaderTableOrColumn(""), 5,
        new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn viewerColumn) {
            return viewerColumn.getDisplayName();
          }
        }));

    selectionColumnPanel.getSelectionModel().addSelectionChangeHandler(event -> {
      if (selectionColumnPanel.getDisplay() != null) {
        selectionColumnPanel.getDisplay().redrawHeaders();
      }

      columnsToInclude.clear();
      columnsToInclude.addAll(selectionColumnPanel.getSelectionModel().getSelectedSet());
    });

    return selectionColumnPanel;
  }

  private Column<ViewerColumn, Boolean> getCheckboxColumn(ViewerTable viewerTable,
    MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel) {
    Column<ViewerColumn, Boolean> column = new Column<ViewerColumn, Boolean>(new CheckboxCell(false, false)) {
      @Override
      public Boolean getValue(ViewerColumn viewerColumn) {
        return selectionTablePanel.getSelectionModel().isSelected(viewerColumn);
      }
    };

    column.setFieldUpdater((i, viewerColumn, aBoolean) -> {
      selectionTablePanel.getSelectionModel().setSelected(viewerColumn, aBoolean);
    });

    return column;
  }

  private Header<Boolean> getSelectAllHeader(ViewerTable viewerTable,
    MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel) {

    Header<Boolean> selectAllHeader = new Header<Boolean>(new com.google.gwt.cell.client.CheckboxCell(true, false)) {
      @Override
      public Boolean getValue() {
        if (viewerTable.getColumns().isEmpty()) {
          return false;
        }
        for (ViewerColumn column : viewerTable.getColumns()) {
          if (!selectionTablePanel.getSelectionModel().isSelected(column)) {
            return false;
          }
        }
        return true;
      }
    };

    selectAllHeader.setUpdater(new ValueUpdater<Boolean>() {
      @Override
      public void update(Boolean isSelected) {
        for (ViewerColumn column : viewerTable.getColumns()) {
          selectionTablePanel.getSelectionModel().setSelected(column, isSelected);
        }
      }
    });

    return selectAllHeader;
  }

  private void resetVirtualFields() {
    templateSourceColumns.clear();
    columnsToInclude.clear();
  }

  private void showError(Widget input, Label errorLabel, String message) {
    input.addStyleName("dialog-input-error");
    errorLabel.setText(message);
    errorLabel.setVisible(true);
  }

  private void clearError(Widget input, Label errorLabel) {
    input.removeStyleName("dialog-input-error");
    errorLabel.setText("");
    errorLabel.setVisible(false);
  }

  public ViewerTable getSimpleViewerTable(String uuid) {
    ViewerTable viewerTable = new ViewerTable();
    viewerTable.setUuid(uuid);
    viewerTable.setName(virtualTableName.getText());
    viewerTable.setDescription(virtualTableDescription.getText());
    ViewerTable sourceViewerTable = database.getMetadata().getTable(sourceTableListBox.getSelectedValue());
    viewerTable.setSchemaUUID(sourceViewerTable.getSchemaUUID());
    return viewerTable;
  }

  public TableStatus getTableStatus() {
    TableStatus statusToReturn = new TableStatus();

    if (ViewerStringUtils.isBlank(statusToReturn.getId())) {
      String uuid = "table_virtual_" + UUID.randomUUID().toString() + ViewerConstants.SOLR_DYN_STRING;
      statusToReturn.setId(uuid);
      statusToReturn.setUuid(uuid);
    }

    statusToReturn.setName(virtualTableName.getText());
    statusToReturn.setCustomName(virtualTableName.getText());
    statusToReturn.setDescription(virtualTableDescription.getText());
    statusToReturn.setCustomDescription(virtualTableDescription.getText());

    ArrayList<ColumnStatus> columnStatuses = new ArrayList<>();
    for (ViewerColumn viewerColumn : columnsToInclude) {
      ColumnStatus columnStatus = collectionStatus.getColumnByTableAndColumn(sourceTableListBox.getSelectedValue(),
        viewerColumn.getSolrName());
      columnStatuses.add(columnStatus);
    }

    statusToReturn.setColumns(columnStatuses);

    VirtualTableStatus virtualTableStatus = new VirtualTableStatus();
    virtualTableStatus.setProcessingState(ProcessingState.TO_PROCESS);

    statusToReturn.setVirtualTableStatus(virtualTableStatus);

    return statusToReturn;
  }

  @Override
  public TemplateStatus getSearchTemplate() {
    return null;
  }

  @Override
  public TemplateStatus getDetailsTemplate() {
    return null;
  }

  @Override
  public TemplateStatus getExportTemplate() {
    return null;
  }
}
