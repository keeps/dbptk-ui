package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.virtual;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ColumnOptionsPanel;
import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ValidatableOptionsPanel;
import com.databasepreservation.common.client.models.status.collection.AdvancedStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.DetailsStatus;
import com.databasepreservation.common.client.models.status.collection.ExportStatus;
import com.databasepreservation.common.client.models.status.collection.ListStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.SearchStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.models.status.collection.VirtualColumnStatus;
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
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class VirtualColumnOptionsPanel extends ColumnOptionsPanel implements ValidatableOptionsPanel {

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

    virtualColumnName.addKeyUpHandler(event -> clearError(virtualColumnName, errorVirtualColumnName));

    templateSourceColumns.addKeyUpHandler(event -> clearError(templateSourceColumns, errorTemplateSourceColumns));

    virtualColumnName.addValueChangeHandler(event -> {
      String val = virtualColumnName.getText();
      if (val.contains(" ")) {
        virtualColumnName.setText(val.replace(" ", "_"));
      }
    });
  }

  private void populateVirtualColumnFields(ColumnStatus columnStatus) {
    if (!ViewerStringUtils.isBlank(columnStatus.getName())) {
      virtualColumnName.setText(columnStatus.getName());
    }

    if (columnStatus.getDescription() != null) {
      virtualColumnDescription.setText(columnStatus.getDescription());
    }

    if (columnStatus.getVirtualColumnStatus() != null) {
      VirtualColumnStatus virtualColumnStatus = columnStatus.getVirtualColumnStatus();
      if (virtualColumnStatus.getSourceColumnsIds() != null) {
        sourceColumnsIds = virtualColumnStatus.getSourceColumnsIds();
      }
      if (virtualColumnStatus.getTemplateStatus() != null) {
        templateSourceColumns.setText(virtualColumnStatus.getTemplateStatus().getTemplate());
      }
    }
  }

  @Override
  public boolean validate() {
    boolean isValid = true;

    clearError(virtualColumnName, errorVirtualColumnName);
    clearError(templateSourceColumns, errorTemplateSourceColumns);

    String nameValue = virtualColumnName.getText();

    if (ViewerStringUtils.isBlank(nameValue)) {
      showError(virtualColumnName, errorVirtualColumnName,
        messages.columnManagementLabelForVirtualColumnName() + " is required.");
      isValid = false;
    } else if (nameValue.contains(" ")) {
      showError(virtualColumnName, errorVirtualColumnName, "Column name cannot contain spaces.");
      isValid = false;
    } else if (!nameValue.matches("^[a-zA-Z0-9_]+$")) {
      showError(virtualColumnName, errorVirtualColumnName, "Only letters, numbers and underscores are allowed.");
      isValid = false;
    }

    if (ViewerStringUtils.isBlank(templateSourceColumns.getText())) {
      showError(templateSourceColumns, errorTemplateSourceColumns,
        messages.columnManagementLabelForSourceColumnsTemplate() + " is required.");
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

  public ColumnStatus getColumnStatus() {
    ColumnStatus statusToReturn = (originalStatus != null) ? originalStatus : new ColumnStatus();

    if (ViewerStringUtils.isBlank(statusToReturn.getId())) {
      String uuid = UUID.randomUUID().toString();
      statusToReturn.setId(
        ViewerConstants.SOLR_INDEX_ROW_COLUMN_NAME_PREFIX + "_virtual_" + uuid + ViewerConstants.SOLR_DYN_STRING);
      statusToReturn.setOrder(tableStatus.getLastColumnOrder() + 1);
    }

    statusToReturn.setType(ViewerType.dbTypes.VIRTUAL);
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
