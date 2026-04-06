/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.browse.configuration.textExtraction;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.databasepreservation.common.client.ObserverManager;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.DefaultMethodCallback;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbItem;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.widgets.MultipleSelectionTablePanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.common.utils.html.LabelUtils;
import com.databasepreservation.common.client.common.visualization.browse.configuration.ConfigurationStatusPanel;
import com.databasepreservation.common.client.configuration.observer.CollectionObserver;
import com.databasepreservation.common.client.models.status.collection.CollectionStatus;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.LobTextExtractionStatus;
import com.databasepreservation.common.client.models.status.collection.ProcessingState;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerColumn;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.widgets.ConfigurationCellTableResources;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionModel;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TextExtractionPanel extends ContentPanel {

  interface TableManagementPanelUiBinder extends UiBinder<Widget, TextExtractionPanel> {
  }

  private static final TableManagementPanelUiBinder binder = GWT.create(TableManagementPanelUiBinder.class);
  private static final Map<String, TextExtractionPanel> instances = new HashMap<>();
  private final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ViewerDatabase database;
  private final CollectionStatus collectionStatus;
  private final Button btnSave = new Button();
  private final Map<String, MultipleSelectionTablePanel<ViewerColumn>> tables = new HashMap<>();
  private final Map<String, Set<String>> initialLoading = new HashMap<>();

  @UiField
  FlowPanel header;

  @UiField
  FlowPanel content;

  @UiField
  ConfigurationStatusPanel configurationStatusPanel;

  private TextExtractionPanel(ViewerDatabase database, CollectionStatus collectionStatus) {
    initWidget(binder.createAndBindUi(this));
    this.database = database;
    this.collectionStatus = collectionStatus;
    configurationStatusPanel.setDatabase(database);

    init();
  }

  public static TextExtractionPanel getInstance(ViewerDatabase database, CollectionStatus status) {
    return instances.computeIfAbsent(database.getUuid(), k -> new TextExtractionPanel(database, status));
  }

  private void init() {
    configureHeader();

    for (ViewerSchema schema : database.getMetadata().getSchemas()) {
      for (ViewerTable table : schema.getTables()) {
        if (!table.getBinaryColumns().isEmpty()) {
          MultipleSelectionTablePanel<ViewerColumn> columnsTable = createCellTableForViewerTable();
          tables.put(table.getUuid(), columnsTable);
          initialLoading.put(table.getUuid(), new HashSet<>());
          columnsTable.setHeight("100%");
          populateTable(columnsTable, table);
          FlowPanel widgets = CommonClientUtils.wrapOnDiv("table-management-schema-divider",
            CommonClientUtils.getHeader(SafeHtmlUtils.fromSafeConstant(schema.getName() + " - " + table.getName()),
              "table-management-schema-title"),
            columnsTable);
          content.add(widgets);
        }
      }
    }

    configureButtonsPanel();
  }

  private void configureButtonsPanel() {
    Button btnCancel = new Button();
    btnCancel.setText(messages.basicActionCancel());
    btnCancel.addStyleName("btn btn-danger btn-times-circle");
    btnSave.setText(messages.basicActionSave());
    btnSave.addStyleName("btn btn-primary btn-save");

    btnCancel.addClickHandler(event -> HistoryManager.gotoAdvancedConfiguration(database.getUuid()));

    btnSave.addClickHandler(clickEvent -> {
      Dialogs.showConfirmDialog(messages.textExtractionWarningTitle(), messages.textExtractionWarningMessage(),
        messages.basicActionCancel(), messages.basicActionProceed(), new AsyncCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean result) {
            if (Boolean.TRUE.equals(result)) {
              saveTextExtractionChanges();
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            // Optionally handle dialog failure (e.g., log or ignore)
          }
        });
    });

    content.add(CommonClientUtils.wrapOnDiv("navigation-panel-buttons",
      CommonClientUtils.wrapOnDiv("btn-item", btnSave), CommonClientUtils.wrapOnDiv("btn-item", btnCancel)));
  }

  private void saveTextExtractionChanges() {
    Date now = new Date();

    for (String tableUUID : initialLoading.keySet()) {
      processExtractionTable(tableUUID, now);
    }

    collectionStatus.setNeedsToBeProcessed(true);
    callUpdateCollectionConfigurationRPC();
  }

  // Processes an individual table
  private void processExtractionTable(String tableUUID, Date now) {
    SelectionModel<ViewerColumn> selectedColumns = tables.get(tableUUID).getSelectionModel();
    TableStatus tableStatus = collectionStatus.getTableStatus(tableUUID);

    for (ViewerColumn lobColumn : database.getMetadata().getTable(tableUUID).getBinaryColumns()) {
      processLobColumnExtraction(lobColumn, selectedColumns, tableStatus, now);
    }
  }

  // Evaluates and updates the extraction status for a single LOB column
  private void processLobColumnExtraction(ViewerColumn lobColumn, SelectionModel<ViewerColumn> selectedColumns,
    TableStatus tableStatus, Date now) {
    boolean isSelected = selectedColumns.isSelected(lobColumn);
    ColumnStatus colStatus = tableStatus.getColumnById(lobColumn.getSolrName());
    LobTextExtractionStatus status = colStatus.getLobTextExtractionStatus();

    boolean currentState = isCurrentlyExtracting(status);

    if (currentState != isSelected) {
      updateLobExtractionStatus(colStatus, status, isSelected, now);
    }
  }

  // Checks current processing state
  private boolean isCurrentlyExtracting(LobTextExtractionStatus status) {
    if (status == null || status.getProcessingState() == null) {
      return false;
    }

    ProcessingState state = status.getProcessingState();

    if (ProcessingState.TO_REMOVE.equals(state)) {
      return false;
    }

    // Must strictly match the visual representation logic (determineCheckboxState)
    return status.getExtractedAndIndexedText() || ProcessingState.TO_PROCESS.equals(state)
      || ProcessingState.PROCESSING.equals(state) || ProcessingState.PENDING_METADATA.equals(state)
      || ProcessingState.FAILED.equals(state);
  }

  // Updates the column status state
  private void updateLobExtractionStatus(ColumnStatus colStatus, LobTextExtractionStatus status, boolean isSelected,
    Date now) {
    LobTextExtractionStatus activeStatus = status;

    if (activeStatus == null) {
      activeStatus = new LobTextExtractionStatus();
      colStatus.setLobTextExtractionStatus(activeStatus);
    }

    activeStatus.setLastUpdatedDate(now);

    if (isSelected) {
      activeStatus.setProcessingState(ProcessingState.TO_PROCESS);
    } else {
      activeStatus.setProcessingState(ProcessingState.TO_REMOVE);
    }
  }

  private void callUpdateCollectionConfigurationRPC() {
    CollectionService.Util.callDetailed((Boolean result) -> {
      final CollectionObserver collectionObserver = ObserverManager.getCollectionObserver();
      collectionObserver.setCollectionStatus(collectionStatus);
    }, errorMessage -> {
      Dialogs.showConfigurationDependencyErrors(errorMessage.get(DefaultMethodCallback.MESSAGE_KEY),
        errorMessage.get(DefaultMethodCallback.DETAILS_KEY), messages.basicActionClose());
    }).updateCollectionConfiguration(database.getUuid(), database.getUuid(), collectionStatus);
  }

  private void configureHeader() {
    header.add(CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE),
      messages.textExtractionPageTitle(), "h1"));

    MetadataField instance = MetadataField.createInstance(messages.textExtractionPageTableTextForDescription());
    instance.setCSS("table-row-description", "font-size-description");
    content.add(instance);
  }

  private MultipleSelectionTablePanel<ViewerColumn> createCellTableForViewerTable() {
    return new MultipleSelectionTablePanel<>(GWT.create(ConfigurationCellTableResources.class));
  }

  private void populateTable(MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel,
    final ViewerTable viewerTable) {

    selectionTablePanel.createTable(new FlowPanel(), Arrays.asList(1, 2), viewerTable.getBinaryColumns().iterator(),
      // Checkbox: Represents the user intention
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.textExtractionPageTableHeaderTextForExtractPolicy(), 10,
        getCheckboxColumn(selectionTablePanel, viewerTable.getUuid())),

      // Status Column: Represents the physical result
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.batchJobsTextForStatus(), 10,
        getStatusLabelColumn(viewerTable.getUuid())),

      new MultipleSelectionTablePanel.ColumnInfo<>(messages.basicTableHeaderTableOrColumn("name"), 15,
        new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn column) {
            return column.getDisplayName();
          }
        }),
      new MultipleSelectionTablePanel.ColumnInfo<>(messages.tableManagementPageTableHeaderTextForDescription(), 0,
        new TextColumn<ViewerColumn>() {
          @Override
          public String getValue(ViewerColumn column) {
            return column.getDescription();
          }
        }));
  }

  private Column<ViewerColumn, Boolean> getCheckboxColumn(MultipleSelectionTablePanel<ViewerColumn> selectionTablePanel,
    String tableUUID) {
    Column<ViewerColumn, Boolean> column = new Column<ViewerColumn, Boolean>(new CheckboxCell(false, false)) {
      @Override
      public Boolean getValue(ViewerColumn viewerColumn) {
        if (!initialLoading.get(tableUUID).contains(viewerColumn.getSolrName())) {
          initialLoading.get(tableUUID).add(viewerColumn.getSolrName());

          ColumnStatus colStatus = collectionStatus.getTableStatus(tableUUID).getColumnById(viewerColumn.getSolrName());

          boolean isExtractEnabled = determineCheckboxState(colStatus);

          selectionTablePanel.getSelectionModel().setSelected(viewerColumn, isExtractEnabled);
        }

        return selectionTablePanel.getSelectionModel().isSelected(viewerColumn);
      }
    };

    column.setFieldUpdater((i, viewerColumn, value) -> {
      selectionTablePanel.getSelectionModel().setSelected(viewerColumn, value);
    });

    return column;
  }

  private Column<ViewerColumn, SafeHtml> getStatusLabelColumn(String tableUUID) {
    return new Column<ViewerColumn, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(ViewerColumn viewerColumn) {
        ColumnStatus colStatus = collectionStatus.getTableStatus(tableUUID).getColumnById(viewerColumn.getSolrName());
        if (colStatus == null || colStatus.getLobTextExtractionStatus() == null) {
          return SafeHtmlUtils.fromSafeConstant("<span class='label-default'>Not Indexed</span>");
        }

        return LabelUtils.getLobExtractionStatusLabel(colStatus.getLobTextExtractionStatus());
      }
    };
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    List<BreadcrumbItem> breadcrumbItems = BreadcrumbManager.forTextExtraction(database.getUuid(),
      database.getMetadata().getName());
    BreadcrumbManager.updateBreadcrumb(breadcrumb, breadcrumbItems);
  }

  /**
   * The checkbox remains selected if the column has a state and is not marked for
   * removal. This preserves intention even if the job results in "No Text Found".
   */
  private boolean determineCheckboxState(ColumnStatus colStatus) {
    if (colStatus == null || colStatus.getLobTextExtractionStatus() == null) {
      return false;
    }

    ProcessingState state = colStatus.getLobTextExtractionStatus().getProcessingState();
    return state != null && !ProcessingState.TO_REMOVE.equals(state);
  }
}
