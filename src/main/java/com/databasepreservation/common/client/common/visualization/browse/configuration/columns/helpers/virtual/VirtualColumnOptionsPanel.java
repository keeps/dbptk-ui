/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.SavableOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ValidatableOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ValidationUiUtils;
import com.databasepreservation.common.client.models.status.collection.AdvancedStatus;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.DetailsStatus;
import com.databasepreservation.common.client.models.status.collection.ExportStatus;
import com.databasepreservation.common.client.models.status.collection.ListStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.SearchStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
import com.databasepreservation.common.client.models.structure.ViewerSourceType;
import com.databasepreservation.common.client.models.structure.ViewerType;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * Panel responsible for creating and editing Virtual Columns.
 * <p>
 * <b>Architecture Note:</b> This panel implements Strict Type Inference. It
 * evaluates the user-defined Handlebars template to determine the correct Solr
 * dynamic field suffix (e.g., _l for Long, _t_sort for String) avoiding
 * strict-typing crashes in the backend indexer.
 *
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnOptionsPanel extends ColumnOptionsPanel
  implements ValidatableOptionsPanel, SavableOptionsPanel {

  interface VirtualColumnOptionsPanelUiBinder extends UiBinder<Widget, VirtualColumnOptionsPanel> {
  }

  private static VirtualColumnOptionsPanelUiBinder binder = GWT.create(VirtualColumnOptionsPanelUiBinder.class);

  private final TableStatus tableStatus;
  private final ColumnStatus originalStatus;
  private List<String> sourceColumnsIds = new ArrayList<>();

  @UiField
  ClientMessages messages;
  @UiField
  TextBox virtualColumnName, virtualColumnDescription, templateSourceColumns;
  @UiField
  Label errorVirtualColumnName, errorTemplateSourceColumns;
  @UiField
  FlowPanel templateSourceColumnsHint;

  private Runnable immediateRemovalCallback;

  public static VirtualColumnOptionsPanel createInstance(TableStatus tableStatus, ColumnStatus columnStatus) {
    return new VirtualColumnOptionsPanel(tableStatus, columnStatus);
  }

  private VirtualColumnOptionsPanel(TableStatus tableStatus, ColumnStatus columnStatus) {
    initWidget(binder.createAndBindUi(this));
    this.tableStatus = tableStatus;
    this.originalStatus = columnStatus;

    VirtualOptionsPanelUtils.renderColumnTemplateButtons(tableStatus.getColumns(), templateSourceColumnsHint,
      templateSourceColumns, sourceColumnsIds, messages, false);

    bindEvents();
    populateVirtualColumnFields(columnStatus);
  }

  private void bindEvents() {
    virtualColumnName.addKeyPressHandler(event -> {
      if (event.getCharCode() == ' ') {
        event.preventDefault();
      }
    });

    virtualColumnName.addKeyUpHandler(event -> ValidationUiUtils.clearError(virtualColumnName, errorVirtualColumnName));

    templateSourceColumns
      .addKeyUpHandler(event -> ValidationUiUtils.clearError(templateSourceColumns, errorTemplateSourceColumns));

    virtualColumnName.addValueChangeHandler(event -> {
      String val = virtualColumnName.getText();
      if (val.contains(" ")) {
        virtualColumnName.setText(val.replace(" ", "_"));
      }
    });
  }

  private void populateVirtualColumnFields(ColumnStatus originalStatus) {
    if (originalStatus != null) {
      if (!ViewerStringUtils.isBlank(originalStatus.getName())) {
        virtualColumnName.setText(originalStatus.getName());
        virtualColumnName.setReadOnly(true);
      }

      if (originalStatus.getDescription() != null) {
        virtualColumnDescription.setText(originalStatus.getDescription());
      }

      if (originalStatus.getVirtualColumnStatus() != null) {
        VirtualColumnStatus virtualColumnStatus = originalStatus.getVirtualColumnStatus();
        if (virtualColumnStatus.getSourceColumnsIds() != null) {
          sourceColumnsIds = virtualColumnStatus.getSourceColumnsIds();
        }
        if (virtualColumnStatus.getTemplateStatus() != null) {
          templateSourceColumns.setText(virtualColumnStatus.getTemplateStatus().getTemplate());
        }
      }
    }
  }

  /**
   * Infers the database type strictly based on the provided Handlebars template.
   * <p>
   * If the template mixes text with variables (e.g., "{{actor_id}} - {{name}}"),
   * the resulting type is forced to STRING to prevent Solr
   * NumberFormatExceptions. It only inherits native numeric/date types if the
   * template is an exact 1:1 mapping.
   *
   * @return The strictly inferred ViewerType.
   */
  private ViewerType.dbTypes inferStrictType() {
    String templateText = templateSourceColumns.getText().trim();

    if (sourceColumnsIds != null && sourceColumnsIds.size() == 1) {
      ColumnStatus sourceColumn = tableStatus.getColumns().stream()
        .filter(col -> col.getId().equals(sourceColumnsIds.get(0))).findFirst().orElse(null);

      if (sourceColumn != null) {
        // Inherit native type ONLY if the template is exclusively the variable
        // placeholder
        String exactPlaceholder = ViewerConstants.OPEN_TEMPLATE_ENGINE + sourceColumn.getName()
          + ViewerConstants.CLOSE_TEMPLATE_ENGINE;
        if (templateText.equals(exactPlaceholder)) {
          return sourceColumn.getType();
        }
      }
    }
    // Any template with multiple variables or static text must default to STRING
    return ViewerType.dbTypes.STRING;
  }

  @Override
  public boolean validate() {
    boolean isValid = true;
    String nameValue = virtualColumnName.getText();

    ValidationUiUtils.clearError(virtualColumnName, errorVirtualColumnName);
    ValidationUiUtils.clearError(templateSourceColumns, errorTemplateSourceColumns);

    // Validate column name (Mutually exclusive conditions)
    if (ViewerStringUtils.isBlank(nameValue)) {
      ValidationUiUtils.showError(virtualColumnName, errorVirtualColumnName,
        messages.columnManagementErrorVirtualColumnNameRequired());
      isValid = false;
    } else if (nameValue.contains(" ")) {
      ValidationUiUtils.showError(virtualColumnName, errorVirtualColumnName,
        messages.columnManagementErrorVirtualColumnNameNoSpaces());
      isValid = false;
    } else if (!nameValue.matches("^[a-zA-Z0-9_]+$")) {
      ValidationUiUtils.showError(virtualColumnName, errorVirtualColumnName,
        messages.columnManagementErrorVirtualColumnNameInvalidChars());
      isValid = false;
    } else if (isDuplicateColumnName(nameValue)) {
      ValidationUiUtils.showError(virtualColumnName, errorVirtualColumnName,
        messages.columnManagementErrorVirtualColumnNameDuplicate());
      isValid = false;
    }

    // Validate template logic
    if (ViewerStringUtils.isBlank(templateSourceColumns.getText())) {
      ValidationUiUtils.showError(templateSourceColumns, errorTemplateSourceColumns,
        messages.columnManagementErrorVirtualColumnTemplateRequired());
      isValid = false;
    } else if (hasTypeMismatchError()) {
      ValidationUiUtils.showError(templateSourceColumns, errorTemplateSourceColumns,
        messages.columnManagementErrorVirtualColumnTypeMismatch());
      isValid = false;
    }

    return isValid;
  }

  private boolean isDuplicateColumnName(String nameValue) {
    boolean isDuplicate = false;
    for (ColumnStatus column : tableStatus.getColumns()) {
      if (column.getName().equals(nameValue)
        && (originalStatus == null || !column.getId().equals(originalStatus.getId()))) {
        isDuplicate = true;
      }
    }
    return isDuplicate;
  }

  private boolean hasTypeMismatchError() {
    boolean hasMismatch = false;
    if (originalStatus != null && originalStatus.getType() != null) {
      ViewerType.dbTypes inferredType = inferStrictType();
      String oldSuffix = VirtualOptionsPanelUtils.getVirtualColumnSolrSuffix(originalStatus.getType());
      String newSuffix = VirtualOptionsPanelUtils.getVirtualColumnSolrSuffix(inferredType);

      if (!oldSuffix.equals(newSuffix)) {
        hasMismatch = true;
      }
    }
    return hasMismatch;
  }

  public ColumnStatus getColumnStatus() {
    ColumnStatus statusToReturn = (originalStatus != null) ? originalStatus : new ColumnStatus();

    ViewerType.dbTypes type = inferStrictType();

    // If editing an existing column, force the original type to prevent orphaning
    // the existing Solr ID
    if (originalStatus != null && originalStatus.getType() != null) {
      type = originalStatus.getType();
    }

    if (ViewerStringUtils.isBlank(statusToReturn.getId())) {
      String uuid = UUID.randomUUID().toString();
      // Appends the strictly inferred suffix to the Solr ID (e.g., _l, _t_sort)
      statusToReturn.setId(ViewerConstants.SOLR_INDEX_ROW_COLUMN_NAME_PREFIX + "_virtual_" + uuid
        + VirtualOptionsPanelUtils.getVirtualColumnSolrSuffix(type));
      statusToReturn.setOrder(tableStatus.getLastColumnOrder() + 1);
    }

    statusToReturn.setType(type);
    statusToReturn.setSourceType(ViewerSourceType.VIRTUAL);
    statusToReturn.setName(virtualColumnName.getText());
    statusToReturn.setCustomName(virtualColumnName.getText());
    statusToReturn.setDescription(virtualColumnDescription.getText());
    statusToReturn.setCustomDescription(virtualColumnDescription.getText());

    ensureStatusStructures(statusToReturn);
    statusToReturn.setVirtualColumnStatus(getVirtualColumnStatus());

    return statusToReturn;
  }

  private void ensureStatusStructures(ColumnStatus status) {
    if (status.getExportStatus() == null)
      status.setExportStatus(new ExportStatus());
    if (status.getExportStatus().getTemplateStatus() == null)
      status.getExportStatus().setTemplateStatus(new TemplateStatus());
    if (status.getSearchStatus() == null)
      status.setSearchStatus(new SearchStatus());
    if (status.getSearchStatus().getAdvanced() == null)
      status.getSearchStatus().setAdvanced(new AdvancedStatus());
    if (status.getSearchStatus().getList() == null) {
      status.getSearchStatus().setList(new ListStatus());
      status.getSearchStatus().getList().setShow(true);
    }
    if (status.getSearchStatus().getList().getTemplate() == null)
      status.getSearchStatus().getList().setTemplate(new TemplateStatus());
    if (status.getDetailsStatus() == null) {
      status.setDetailsStatus(new DetailsStatus());
      status.getDetailsStatus().setShow(true);
    }
    if (status.getDetailsStatus().getTemplateStatus() == null)
      status.getDetailsStatus().setTemplateStatus(new TemplateStatus());
  }

  @NotNull
  private VirtualColumnStatus getVirtualColumnStatus() {
    VirtualColumnStatus virtualColumnStatus = new VirtualColumnStatus();
    virtualColumnStatus.setSourceColumnsIds(sourceColumnsIds);
    TemplateStatus sourceTemplateStatus = new TemplateStatus();
    sourceTemplateStatus.setTemplate(templateSourceColumns.getText());
    virtualColumnStatus.setTemplateStatus(sourceTemplateStatus);
    virtualColumnStatus.setProcessingState(ProcessingState.TO_PROCESS);
    return virtualColumnStatus;
  }

  @Override
  public boolean hasChanges() {
    if (originalStatus == null || ViewerStringUtils.isBlank(originalStatus.getName())) {
      return true; // New column
    }

    boolean nameChanged = !virtualColumnName.getText().equals(originalStatus.getName());
    boolean descChanged = !virtualColumnDescription.getText().equals(originalStatus.getDescription());
    boolean templateChanged = !templateSourceColumns.getText()
      .equals(originalStatus.getVirtualColumnStatus().getTemplateStatus().getTemplate());

    return nameChanged || descChanged || templateChanged;
  }

  @Override
  public void applyChanges(ColumnStatus columnStatus, TableStatus tableStatus, CollectionStatus collectionStatus) {
    ColumnStatus updated = getColumnStatus();

    // Update basic metadata
    columnStatus.setName(updated.getName());
    columnStatus.setCustomName(updated.getCustomName());
    columnStatus.setDescription(updated.getDescription());
    columnStatus.setCustomDescription(updated.getCustomDescription());

    // Update virtual technical status
    columnStatus.setVirtualColumnStatus(updated.getVirtualColumnStatus());
    columnStatus.getVirtualColumnStatus().setProcessingState(ProcessingState.TO_PROCESS);
    columnStatus.getVirtualColumnStatus().setLastUpdatedDate(new Date());

    // Ensure Solr ID is consistent with type if it's a new column
    if (ViewerStringUtils.isBlank(columnStatus.getId())) {
      columnStatus.setId(updated.getId());
      columnStatus.setType(updated.getType());
      columnStatus.setSourceType(updated.getSourceType());
    }
  }

  @Override
  public boolean requiresProcessing() {
    return true;
  }

  public void setImmediateRemovalCallback(Runnable callback) {
    this.immediateRemovalCallback = callback;
  }
}
