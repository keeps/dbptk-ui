package com.databasepreservation.common.client.common.visualization.browse.configuration.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.databasepreservation.common.client.common.lists.cells.DisableableCheckboxCell;
import com.databasepreservation.common.client.common.lists.cells.helper.CheckboxData;
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
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.CheckBox;
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
  private final TableStatus originalStatus;
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
  FlowPanel sourceColumnsTable, sourceColumnsOptionsPanel;
  @UiField
  CheckBox includeSourceForeignKeys;

  public static VirtualTableOptionsPanel createInstance(ViewerDatabase database, CollectionStatus collectionStatus,
    TableStatus originalStatus) {
    return new VirtualTableOptionsPanel(database, collectionStatus, originalStatus);
  }

  private VirtualTableOptionsPanel(ViewerDatabase database, CollectionStatus collectionStatus,
    TableStatus originalStatus) {
    initWidget(binder.createAndBindUi(this));
    this.collectionStatus = collectionStatus;
    this.database = database;
    this.originalStatus = originalStatus;

    setSourceTablesDropdown();
    bindEvents();

    populateVirtualTableFields(originalStatus);
  }

  private void bindEvents() {
    virtualTableName.addKeyPressHandler(event -> {
      if (event.getCharCode() == ' ') {
        event.preventDefault();
      }
    });

    virtualTableName.addKeyUpHandler(event -> clearError(virtualTableName, errorVirtualTableName));

    sourceTableListBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        clearError(sourceTableListBox, errorSourceTable);
        onSourceTableChanged();
      }
    });
  }

  private void populateVirtualTableFields(TableStatus originalStatus) {
    if (originalStatus != null) {
      if (!ViewerStringUtils.isBlank(originalStatus.getName())) {
        virtualTableName.setText(originalStatus.getName());
        virtualTableName.setReadOnly(true);
      }

      if (originalStatus.getDescription() != null) {
        virtualTableDescription.setText(originalStatus.getDescription());
      }

      if (originalStatus.getVirtualTableStatus() != null) {
        String sourceTableUUID = originalStatus.getVirtualTableStatus().getSourceTableUUID();
        if (ViewerStringUtils.isNotBlank(sourceTableUUID)) {
          for (int i = 0; i < sourceTableListBox.getItemCount(); i++) {
            if (sourceTableListBox.getValue(i).equals(sourceTableUUID)) {
              sourceTableListBox.setSelectedIndex(i);
              onSourceTableChanged();
              break;
            }
          }
          sourceTableListBox.setEnabled(false);
        }

        includeSourceForeignKeys.setValue(originalStatus.getVirtualTableStatus().getUseSourceTableForeignKeys());
        includeSourceForeignKeys.setEnabled(false);
      }
    }
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
    for (TableStatus table : collectionStatus.getTables()) {
      if (table.getVirtualTableStatus() == null) {
        sourceTableListBox.addItem(table.getName(), table.getUuid());
      }
    }
  }

  private void onSourceTableChanged() {
    String selectedTableId = sourceTableListBox.getSelectedValue();

    if (selectedTableId.isEmpty()) {
      resetVirtualFields();
      sourceColumnsOptionsPanel.setVisible(false);
    } else {
      resetVirtualFields();
      sourceColumnsOptionsPanel.setVisible(true);
      List<ColumnStatus> savedColumns = null;
      if (originalStatus != null && originalStatus.getVirtualTableStatus() != null
        && selectedTableId.equals(originalStatus.getVirtualTableStatus().getSourceTableUUID())) {
        savedColumns = originalStatus.getColumns();
      }
      sourceColumnsTable.add(populateColumnsOptions(selectedTableId, savedColumns));
      sourceColumnsTable.getElement().getStyle().setProperty("maxHeight", "200px");
      sourceColumnsTable.getElement().getStyle().setMarginBottom(10, Style.Unit.PX);
    }
  }

  @NotNull
  private MultipleSelectionTablePanel<ViewerColumn> populateColumnsOptions(String selectedTableId,
    List<ColumnStatus> savedColumns) {
    ViewerTable viewerTable = database.getMetadata().getTable(selectedTableId);
    MultipleSelectionTablePanel<ViewerColumn> selectionColumnPanel = new MultipleSelectionTablePanel<>();

    boolean isReadOnly = originalStatus != null;

    Header<CheckboxData> selectAllHeader = getSelectAllHeader(viewerTable, selectionColumnPanel, isReadOnly);

    List<Integer> whitelistedColumns = isReadOnly ? Collections.singletonList(-1) : new ArrayList<>();

    selectionColumnPanel.createTable(new FlowPanel(), whitelistedColumns, viewerTable.getColumns().iterator(),
      // checkbox
      new MultipleSelectionTablePanel.ColumnInfo<>(selectAllHeader, 1,
        getCheckboxColumn(selectionColumnPanel, isReadOnly)),
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

    if (savedColumns != null && !savedColumns.isEmpty()) {
      Set<String> savedColumnNames = savedColumns.stream().map(ColumnStatus::getId).collect(Collectors.toSet());

      for (ViewerColumn viewerColumn : viewerTable.getColumns()) {
        if (savedColumnNames.contains(viewerColumn.getSolrName())) {
          selectionColumnPanel.getSelectionModel().setSelected(viewerColumn, true);
        }
      }
    }

    return selectionColumnPanel;
  }

  private Column<ViewerColumn, CheckboxData> getCheckboxColumn(
    MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel, boolean isReadOnly) {
    Column<ViewerColumn, CheckboxData> column = new Column<ViewerColumn, CheckboxData>(
      getSafeCheckboxCell(true, false, isReadOnly)) {
      @Override
      public CheckboxData getValue(ViewerColumn viewerColumn) {
        boolean isSelected = selectionTablePanel.getSelectionModel().isSelected(viewerColumn);

        CheckboxData data = new CheckboxData();
        data.setChecked(isSelected);
        data.setDisable(isReadOnly);
        return data;
      }
    };

    if (!isReadOnly) {
      column.setFieldUpdater((i, viewerColumn, checkboxData) -> {
        selectionTablePanel.getSelectionModel().setSelected(viewerColumn, checkboxData.isChecked());
      });
    }

    return column;
  }

  private Header<CheckboxData> getSelectAllHeader(ViewerTable viewerTable,
    MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel, boolean isReadOnly) {

    Header<CheckboxData> selectAllHeader = new Header<CheckboxData>(getSafeCheckboxCell(false, false, isReadOnly)) {
      @Override
      public CheckboxData getValue() {
        boolean allSelected = !viewerTable.getColumns().isEmpty();

        for (ViewerColumn column : viewerTable.getColumns()) {
          if (!selectionTablePanel.getSelectionModel().isSelected(column)) {
            allSelected = false;
            break;
          }
        }

        CheckboxData data = new CheckboxData();
        data.setChecked(allSelected);
        data.setDisable(isReadOnly);
        return data;
      }
    };

    if (!isReadOnly) {
      selectAllHeader.setUpdater(new ValueUpdater<CheckboxData>() {
        @Override
        public void update(CheckboxData value) {
          for (ViewerColumn column : viewerTable.getColumns()) {
            selectionTablePanel.getSelectionModel().setSelected(column, value.isChecked());
          }
        }
      });
    }

    return selectAllHeader;
  }

  private DisableableCheckboxCell getSafeCheckboxCell(boolean dependsOnSelection, boolean handlesSelection,
    boolean isReadOnly) {
    return new DisableableCheckboxCell(dependsOnSelection, handlesSelection) {
      @Override
      public void render(Context context, CheckboxData value, SafeHtmlBuilder safeHtmlBuilder) {
        if (isReadOnly && value != null && value.isChecked()) {
          safeHtmlBuilder
            .appendHtmlConstant("<input type=\"checkbox\" tabindex=\"-1\" checked disabled=\"disabled\"/>");
        } else {
          super.render(context, value, safeHtmlBuilder);
        }
      }

      @Override
      public void onBrowserEvent(Context context, Element parent, CheckboxData value, NativeEvent event,
        ValueUpdater<CheckboxData> valueUpdater) {
        if (isReadOnly) {
          return;
        }
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
      }
    };
  }

  private void resetVirtualFields() {
    sourceColumnsTable.clear();
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
    TableStatus statusToReturn = (originalStatus != null) ? originalStatus : new TableStatus();

    if (ViewerStringUtils.isBlank(statusToReturn.getUuid())) {
      String uuid = "table_virtual_" + UUID.randomUUID().toString();
      statusToReturn.setUuid(uuid);
    }

    ViewerTable sourceViewerTable = database.getMetadata().getTable(sourceTableListBox.getSelectedValue());
    if (ViewerStringUtils.isNotBlank(sourceViewerTable.getSchemaName())) {
      statusToReturn.setId(sourceViewerTable.getSchemaName() + "." + virtualTableName.getText());
    } else {
      statusToReturn.setId(virtualTableName.getText());
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
    virtualTableStatus.setSourceTableUUID(sourceTableListBox.getSelectedValue());
    virtualTableStatus.setUseSourceTableForeignKeys(includeSourceForeignKeys.getValue());

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
