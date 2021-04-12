/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.visualization.progressBar;

import java.util.HashMap;
import java.util.function.Consumer;

import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.models.progress.ProgressData;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ProgressBarPanel extends Composite {
  interface ProgressBarPanelUiBinder extends UiBinder<Widget, ProgressBarPanel> {
  }

  private static final ProgressBarPanelUiBinder uiBinder = GWT.create(ProgressBarPanelUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final HashMap<String, ProgressBarPanel> instances = new HashMap<>();
  private final String databaseUUID;
  private final boolean redirect;
  private boolean initialized = false;
  private final Timer autoUpdateTimer = new Timer() {
    @Override
    public void run() {
      ProgressBarPanel.this.update();
    }
  };

  @UiField
  FlowPanel content;

  @UiField
  Progressbar progressBar;

  @UiField
  SimplePanel header;

  @UiField
  SimplePanel description;

  public static ProgressBarPanel getInstance(String uuid) {
    return ProgressBarPanel.getInstance(uuid, false);
  }

  public static ProgressBarPanel getInstance(String uuid, boolean redirect) {
    return instances.computeIfAbsent(uuid, k -> new ProgressBarPanel(uuid, redirect));
  }

  private ProgressBarPanel(String uuid, boolean redirect) {
    initWidget(uiBinder.createAndBindUi(this));
    this.databaseUUID = uuid;
    this.redirect = redirect;
    update();
  }

  private void init() {
    CollectionService.Util.call((ProgressData result) -> {
      if (!initialized) {
        result.reset();
        initialized = true;
      }

      progressBar.setCurrent(0);
      content.clear();
      stopUpdating();
      autoUpdateTimer.scheduleRepeating(1000);
    }).getProgressData(databaseUUID);
  }

  public void setTitleText(String text) {
    header.setWidget(
      CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.BOX_OPEN), text, "h1"));
  }

  public void setTitleText(String iconTag, String text) {
    header.setWidget(CommonClientUtils.getHeaderHTML(iconTag, text, "h1"));
  }

  public void setSubtitleText(String text) {
    MetadataField instance = MetadataField.createInstance(text);
    instance.setCSS("table-row-description", "font-size-description");

    description.setWidget(instance);
  }

  @Override
  protected void onAttach() {
    init();
    super.onAttach();
  }

  public void clear(String uuid) {
    progressBar.setCurrent(0);
    initialized = false;
    content.clear();
    instances.put(uuid, null);
  }

  private void update() {
    CollectionService.Util.call((Consumer<ProgressData>) this::update).getProgressData(databaseUUID);
  }

  private void update(ProgressData progressData) {
    if (progressData.isDatabaseStructureRetrieved()) {
      int currentGlobalPercent = new Double(
        ((progressData.getProcessedRows() * 1.0D) / progressData.getTotalRows()) * 100).intValue();
      progressBar.setCurrent(currentGlobalPercent);

      final String totalTablesPercentage = ProgressBarUtils.buildPercentageMessage(
        messages.progressBarPanelTextForTables(), progressData.getProcessedTables(), progressData.getTotalTables());
      final String totalRowsPercentage = ProgressBarUtils.buildPercentageMessage(messages.progressBarPanelTextForRows(),
        progressData.getProcessedRows(), progressData.getTotalRows());
      final String currentTable = ProgressBarUtils.buildSimpleMessage(messages.progressBarPanelTextForCurrentTable(),
        progressData.getCurrentTableName());
      final String currentTableRowsPercentage = ProgressBarUtils.buildPercentageMessage(
        messages.progressBarPanelTextForCurrentRows(), progressData.getCurrentProcessedTableRows(),
        progressData.getCurrentTableTotalRows());

      ProgressBarUtils.addMessageToContent(content, 1, totalTablesPercentage);
      ProgressBarUtils.addMessageToContent(content, 2, totalRowsPercentage);
      ProgressBarUtils.addMessageToContent(content, 3, currentTable);
      ProgressBarUtils.addMessageToContent(content, 4, currentTableRowsPercentage);
    } else {
      final String retrieving = ProgressBarUtils.buildSimpleMessage("",
        messages.progressBarPanelTextForRetrievingTableStructure());
      ProgressBarUtils.addMessageToContent(content, 0, retrieving);
    }

    if (progressData.isFinished()) {
      stopUpdating();
      if (redirect) {
        HistoryManager.gotoDatabase(databaseUUID);
        Dialogs.showInformationDialog(messages.SIARDHomePageDialogTitleForBrowsing(),
          messages.SIARDHomePageTextForIngestSuccess(), messages.basicActionClose(), "btn btn-link");
      }
    }
  }

  @Override
  protected void onDetach() {
    stopUpdating();
    super.onDetach();
  }

  private void stopUpdating() {
    autoUpdateTimer.cancel();
  }
}
