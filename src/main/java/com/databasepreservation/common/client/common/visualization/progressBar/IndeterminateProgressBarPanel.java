package com.databasepreservation.common.client.common.visualization.progressBar;

import java.util.HashMap;
import java.util.function.Consumer;

import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.models.progress.ProgressData;
import com.databasepreservation.common.client.services.CollectionService;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
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
public class IndeterminateProgressBarPanel extends Composite {
  interface IndeterminateProgressBarPanelUiBinder extends UiBinder<Widget, IndeterminateProgressBarPanel> {
  }

  private static final IndeterminateProgressBarPanelUiBinder uiBinder = GWT
    .create(IndeterminateProgressBarPanelUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final HashMap<String, IndeterminateProgressBarPanel> instances = new HashMap<>();
  private final String databaseUUID;
  private boolean initialized = false;
  private boolean databaseAlreadyRetrieved = true;
  private final Timer autoUpdateTimer = new Timer() {
    @Override
    public void run() {
      IndeterminateProgressBarPanel.this.update();
    }
  };

  @UiField
  FlowPanel content;

  @UiField
  SimplePanel header;

  @UiField
  SimplePanel progress;

  @UiField
  SimplePanel description;

  public static IndeterminateProgressBarPanel getInstance(String uuid) {
    return instances.computeIfAbsent(uuid, k -> new IndeterminateProgressBarPanel(uuid));
  }

  private IndeterminateProgressBarPanel(String uuid) {
    initWidget(uiBinder.createAndBindUi(this));
    this.databaseUUID = uuid;
    update();
  }

  private void init() {
    CollectionService.Util.call((ProgressData result) -> {
      if (!initialized) {
        result.reset();
        initialized = true;
        progress.setVisible(true);
      }
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
    initialized = false;
    content.clear();
    instances.put(uuid, null);
  }

  private void update() {
    CollectionService.Util.call((Consumer<ProgressData>) this::update).getProgressData(databaseUUID);
  }

  private void update(ProgressData progressData) {
    int startIndex = 0;
    if (progressData.isDatabaseStructureRetrieved()) {
      if (!databaseAlreadyRetrieved) {
        startIndex = 1;
      }

      final String totalTablesPercentage = ProgressBarUtils.buildPercentageMessage(
        messages.progressBarPanelTextForTables(), progressData.getProcessedTables(), progressData.getTotalTables());
      final String currentTable = ProgressBarUtils.buildSimpleMessage(messages.progressBarPanelTextForCurrentTable(),
        progressData.getCurrentTableName());

      final String numberOfRowsProcessed = ProgressBarUtils.buildSimpleMessage(
        messages.progressBarPanelTextForTotalRowsProcess(), Long.toString(progressData.getProcessedRows()));

      ProgressBarUtils.addMessageToContent(content, startIndex++, totalTablesPercentage);
      ProgressBarUtils.addMessageToContent(content, startIndex++, currentTable);
      ProgressBarUtils.addMessageToContent(content, startIndex, numberOfRowsProcessed);
    } else {
      databaseAlreadyRetrieved = false;
      final String retrieving = ProgressBarUtils
          .buildSimpleMessage(messages.progressBarPanelTextForRetrievingTableStructure());
      ProgressBarUtils.addMessageToContent(content, startIndex, retrieving);
    }

    if (progressData.isFinished()) {
      stopUpdating();
      progress.setVisible(false);
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
