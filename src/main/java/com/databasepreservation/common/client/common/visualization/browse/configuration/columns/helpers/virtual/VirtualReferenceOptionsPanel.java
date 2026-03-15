package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ValidatableOptionsPanel;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualForeignKeysStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class VirtualReferenceOptionsPanel extends ColumnOptionsPanel implements ValidatableOptionsPanel {

  interface VirtualReferenceOptionsPanelUiBinder extends UiBinder<Widget, VirtualReferenceOptionsPanel> {
  }

  private static final VirtualReferenceOptionsPanelUiBinder binder = GWT
    .create(VirtualReferenceOptionsPanelUiBinder.class);

  private final CollectionStatus collectionStatus;
  private final TableStatus currentTableStatus;
  private final ColumnStatus currentColumnStatus;
  private final ViewerDatabase database;
  private List<String> targetColumnsIds = new ArrayList<>();

  @UiField
  ClientMessages messages;
  @UiField
  FlowPanel virtualReferencePanel, fkReferencePanel, templateReferencedColumnsHint;
  @UiField
  ListBox referencedTableListBox;
  @UiField
  TextBox templateReferencedColumns;
  @UiField
  Label fkTableLabel, fkColumnsLabel;
  @UiField
  Label errorReferencedTable, errorTemplateReferencedColumns;

  public static VirtualReferenceOptionsPanel createInstance(ViewerDatabase database, CollectionStatus collectionStatus,
    TableStatus tableStatus, ColumnStatus columnStatus, ForeignKeysStatus foreignKeysStatus) {
    return new VirtualReferenceOptionsPanel(database, collectionStatus, tableStatus, columnStatus, foreignKeysStatus);
  }

  private VirtualReferenceOptionsPanel(ViewerDatabase database, CollectionStatus collectionStatus,
    TableStatus tableStatus, ColumnStatus columnStatus, ForeignKeysStatus foreignKeysStatus) {
    initWidget(binder.createAndBindUi(this));

    this.database = database;
    this.collectionStatus = collectionStatus;
    this.currentTableStatus = tableStatus;
    this.currentColumnStatus = columnStatus;

    setupReferenceTableDropdown();
    bindEvents();
    populateVirtualReferenceFields(foreignKeysStatus);
  }

  private void setupReferenceTableDropdown() {
    referencedTableListBox.clear();
    referencedTableListBox.addItem("", "");
    collectionStatus.getTables().forEach(table -> referencedTableListBox.addItem(table.getId(), table.getUuid()));
  }

  private void bindEvents() {
    referencedTableListBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        VirtualReferenceOptionsPanel.this.clearError(referencedTableListBox, errorReferencedTable);
        VirtualReferenceOptionsPanel.this.onReferencedTableChanged();
      }
    });

    templateReferencedColumns.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        VirtualReferenceOptionsPanel.this.clearError(templateReferencedColumns, errorTemplateReferencedColumns);
      }
    });
  }

  private void onReferencedTableChanged() {
    String selectedTableId = referencedTableListBox.getSelectedItemText();
    if (selectedTableId.isEmpty()) {
      resetVirtualFields();
    } else {
      TableStatus targetTable = collectionStatus.getTableStatusByTableId(selectedTableId);
      if (targetTable != null) {
        VirtualOptionsPanelUtils.renderColumnTemplateButtons(targetTable.getColumns(), templateReferencedColumnsHint,
          templateReferencedColumns, targetColumnsIds, messages, true);
      }
    }
  }

  @Override
  public boolean validate() {
    boolean isValid = true;

    clearError(referencedTableListBox, errorReferencedTable);
    clearError(templateReferencedColumns, errorTemplateReferencedColumns);

    if (fkReferencePanel.isVisible()) {
      return true;
    }

    String selectedTable = referencedTableListBox.getSelectedValue();
    boolean hasTable = !ViewerStringUtils.isBlank(selectedTable);
    boolean hasTemplate = !ViewerStringUtils.isBlank(templateReferencedColumns.getText());

    if (hasTemplate && !hasTable) {
      showError(referencedTableListBox, errorReferencedTable,
        messages.columnManagementLabelForReferencedTable() + " is required.");
      isValid = false;
    }

    if (hasTable && !hasTemplate) {
      showError(templateReferencedColumns, errorTemplateReferencedColumns,
        messages.columnManagementLabelForReferencedColumnsTemplate() + " is required.");
      isValid = false;
    }

    return isValid;
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

  private void populateVirtualReferenceFields(ForeignKeysStatus foreignKeysStatus) {
    ViewerTable table = database.getMetadata().getTable(currentTableStatus.getUuid());

    if (foreignKeysStatus == null) {
      showVirtualReferenceEditor(new ForeignKeysStatus());
    } else if (foreignKeysStatus.getSourceType() != null && foreignKeysStatus.getSourceType().equals(ViewerSourceType.VIRTUAL)) {
      showVirtualReferenceEditor(foreignKeysStatus);
    } else {
      showFkInfo(foreignKeysStatus, table);
    }
  }

  private void showFkInfo(ForeignKeysStatus fk, ViewerTable sourceTable) {
    fkReferencePanel.setVisible(true);
    virtualReferencePanel.setVisible(false);

    ViewerTable targetTable = database.getMetadata().getTable(fk.getReferencedTableUUID());
    fkTableLabel.setText(targetTable != null ? targetTable.getId() : "Unknown Table");

    if (sourceTable.getColumns() != null) {
      StringJoiner joiner = new StringJoiner(", ");
      for (ForeignKeysStatus.ReferencedColumnStatus ref : fk.getReferences()) {
        String displayName = collectionStatus.getColumnByTableIdAndColumn(sourceTable.getId(), ref.getSourceColumnId())
          .getCustomName();
        joiner.add(displayName);
      }
      String columnNames = joiner.toString();
      fkColumnsLabel.setText(columnNames);
    }
  }

  private void showVirtualReferenceEditor(ForeignKeysStatus foreignKeysStatus) {
    fkReferencePanel.setVisible(false);
    virtualReferencePanel.setVisible(true);

    VirtualForeignKeysStatus virtualStatus = foreignKeysStatus.getVirtualForeignKeysStatus();
    if (virtualStatus == null)
      return;

    VirtualOptionsPanelUtils.selectListBoxValue(referencedTableListBox, foreignKeysStatus.getReferencedTableUUID());

    onReferencedTableChanged();

    if (virtualStatus.getTemplateStatus() != null) {
      templateReferencedColumns.setText(virtualStatus.getTemplateStatus().getTemplate());
      for (ForeignKeysStatus.ReferencedColumnStatus reference : foreignKeysStatus.getReferences()) {
        targetColumnsIds.add(reference.getReferencedColumnId());
      }
    }
  }

  private void resetVirtualFields() {
    templateReferencedColumns.setText("");
    templateReferencedColumnsHint.clear();
    targetColumnsIds.clear();
  }

  public ForeignKeysStatus getVirtualReferenceStatus() {
    String selectedTableUUID = referencedTableListBox.getSelectedValue();
    String selectedTable = referencedTableListBox.getSelectedItemText();

    if (fkReferencePanel.isVisible() || ViewerStringUtils.isBlank(selectedTable)) {
      return null;
    }

    ForeignKeysStatus foreignKeysStatus = new ForeignKeysStatus();
    foreignKeysStatus.setSourceType(ViewerSourceType.VIRTUAL);
    foreignKeysStatus.setId("virtual_fk_" + selectedTable);
    foreignKeysStatus.setName("virtual_fk_" + selectedTable);
    foreignKeysStatus.setReferencedTableId(selectedTable);
    foreignKeysStatus.setReferencedTableUUID(selectedTableUUID);

    for (String targetColumnsId : targetColumnsIds) {
      ForeignKeysStatus.ReferencedColumnStatus viewerReference = new ForeignKeysStatus.ReferencedColumnStatus();
      viewerReference.setReferencedColumnId(targetColumnsId);
      viewerReference.setSourceColumnId(currentColumnStatus.getId());
      foreignKeysStatus.getReferences().add(viewerReference);
    }

    VirtualForeignKeysStatus status = new VirtualForeignKeysStatus();
    TemplateStatus template = new TemplateStatus();
    template.setTemplate(templateReferencedColumns.getText());
    status.setTemplateStatus(template);

    foreignKeysStatus.setVirtualForeignKeysStatus(status);

    return foreignKeysStatus;
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
