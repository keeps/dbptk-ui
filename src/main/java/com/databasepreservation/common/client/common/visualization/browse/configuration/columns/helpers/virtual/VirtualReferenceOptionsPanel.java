package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual;

import java.util.StringJoiner;

import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ValidatableOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ValidationUiUtils;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.ForeignKeysStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualForeignKeysStatus;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
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

  @UiField
  ClientMessages messages;
  @UiField
  FlowPanel virtualReferencePanel, fkReferencePanel;
  @UiField
  ListBox referencedTableListBox, referencedColumnListBox;
  @UiField
  Label fkTableLabel, fkColumnsLabel;
  @UiField
  Label errorReferencedTable, errorReferencedColumn;
  @UiField
  Label sourceColumnTypeLabel, noCompatibleColumnsMessage;

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

    // Set the source column type label
    if (sourceColumnTypeLabel != null && currentColumnStatus != null && currentColumnStatus.getType() != null) {
      sourceColumnTypeLabel.setText("Source Type: " + currentColumnStatus.getType());
    }
    if (noCompatibleColumnsMessage != null) {
      noCompatibleColumnsMessage.setVisible(false);
    }

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
        ValidationUiUtils.clearError(referencedTableListBox, errorReferencedTable);
        VirtualReferenceOptionsPanel.this.onReferencedTableChanged();
      }
    });

    referencedColumnListBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        ValidationUiUtils.clearError(referencedColumnListBox, errorReferencedColumn);
      }
    });
  }

  private void onReferencedTableChanged() {
    referencedColumnListBox.clear();
    referencedColumnListBox.addItem("", "");

    if (noCompatibleColumnsMessage != null) {
      noCompatibleColumnsMessage.setVisible(false);
    }
    referencedColumnListBox.setVisible(true);

    String selectedTableId = referencedTableListBox.getSelectedItemText();
    int compatibleCount = 0;
    if (!selectedTableId.isEmpty()) {
      TableStatus targetTable = collectionStatus.getTableStatusByTableId(selectedTableId);
      if (targetTable != null) {
        compatibleCount = (int) targetTable.getColumns().stream()
          .filter(c -> VirtualOptionsPanelUtils.isSupportedColumnType(c, true))
          .filter(c -> c.getType() != null && c.getType().equals(currentColumnStatus.getType()))
          .peek(c -> referencedColumnListBox.addItem(c.getName(), c.getId())).count();
      }
      if (compatibleCount == 0) {
        referencedColumnListBox.setVisible(false);
        if (noCompatibleColumnsMessage != null) {
          noCompatibleColumnsMessage.setVisible(true);
        }
      } else {
        referencedColumnListBox.setVisible(true);
        if (noCompatibleColumnsMessage != null) {
          noCompatibleColumnsMessage.setVisible(false);
        }
      }
    } else {
      referencedColumnListBox.setVisible(true);
      if (noCompatibleColumnsMessage != null) {
        noCompatibleColumnsMessage.setVisible(false);
      }
    }
  }

  @Override
  public boolean validate() {
    boolean isValid = true;

    ValidationUiUtils.clearError(referencedTableListBox, errorReferencedTable);
    ValidationUiUtils.clearError(referencedColumnListBox, errorReferencedColumn);

    // Only validate if the user is actually defining a new reference
    if (!fkReferencePanel.isVisible()) {
      String selectedTable = referencedTableListBox.getSelectedValue();
      String selectedColumn = referencedColumnListBox.getSelectedValue();

      boolean hasTable = !ViewerStringUtils.isBlank(selectedTable);
      boolean hasColumn = !ViewerStringUtils.isBlank(selectedColumn);

      if (hasTable && !hasColumn) {
        ValidationUiUtils.showError(referencedColumnListBox, errorReferencedColumn, "Target column is required.");
        isValid = false;
      }
    }

    return isValid;
  }

  private void populateVirtualReferenceFields(ForeignKeysStatus foreignKeysStatus) {
    ViewerTable table = database.getMetadata().getTable(currentTableStatus.getUuid());

    if (foreignKeysStatus == null) {
      showVirtualReferenceEditor(new ForeignKeysStatus());
    } else if (foreignKeysStatus.getSourceType() != null
      && foreignKeysStatus.getSourceType().equals(ViewerSourceType.VIRTUAL)) {
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

    if (foreignKeysStatus.getReferences() != null && !foreignKeysStatus.getReferences().isEmpty()) {
      String targetColId = foreignKeysStatus.getReferences().get(0).getReferencedColumnId();
      VirtualOptionsPanelUtils.selectListBoxValue(referencedColumnListBox, targetColId);
    }
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

    String targetColumnId = referencedColumnListBox.getSelectedValue();
    String targetColumnName = referencedColumnListBox.getSelectedItemText();

    ForeignKeysStatus.ReferencedColumnStatus viewerReference = new ForeignKeysStatus.ReferencedColumnStatus();
    viewerReference.setReferencedColumnId(targetColumnId);
    viewerReference.setSourceColumnId(currentColumnStatus.getId());
    foreignKeysStatus.getReferences().add(viewerReference);

    VirtualForeignKeysStatus status = new VirtualForeignKeysStatus();
    TemplateStatus template = new TemplateStatus();

    template.setTemplate("{{" + targetColumnName + "}}");
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
