package com.databasepreservation.common.client.common.visualization.progressBar;

import java.util.HashMap;
import java.util.function.Consumer;

import com.databasepreservation.common.client.common.Progressbar;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.models.progress.ProgressData;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ProgressBarPanel extends Composite {
  interface ProgressBarPanelUiBinder extends UiBinder<Widget, ProgressBarPanel> {
  }

  private static ProgressBarPanelUiBinder uiBinder = GWT.create(ProgressBarPanelUiBinder.class);

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static HashMap<String, ProgressBarPanel> instances = new HashMap<>();
  private String databaseUUID;
  private boolean initialized = false;
  private Timer autoUpdateTimer = new Timer() {
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
    return instances.computeIfAbsent(uuid, k -> new ProgressBarPanel(uuid));
  }

  private ProgressBarPanel(String uuid) {
    initWidget(uiBinder.createAndBindUi(this));
    this.databaseUUID = uuid;
    update();
  }

  private void init() {
    DatabaseService.Util.call((ProgressData result) -> {
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
    DatabaseService.Util.call((Consumer<ProgressData>) this::update).getProgressData(databaseUUID);
  }

  private void update(ProgressData progressData) {
    if (progressData.isDatabaseStructureRetrieved()) {
      int currentGlobalPercent = new Double(
        ((progressData.getProcessedRows() * 1.0D) / progressData.getTotalRows()) * 100).intValue();
      progressBar.setCurrent(currentGlobalPercent);

      final String totalTablesPercentage = buildPercentageMessage(messages.progressBarPanelTextForTables(),
        progressData.getProcessedTables(), progressData.getTotalTables());
      final String totalRowsPercentage = buildPercentageMessage(messages.progressBarPanelTextForRows(),
        progressData.getProcessedRows(), progressData.getTotalRows());
      final String currentTable = buildSimpleMessage(messages.progressBarPanelTextForCurrentTable(),
        progressData.getCurrentTableName());
      final String currentTableRowsPercentage = buildPercentageMessage(messages.progressBarPanelTextForCurrentRows(),
        progressData.getCurrentProcessedTableRows(), progressData.getCurrentTableTotalRows());

      addMessageToContent(1, totalTablesPercentage);
      addMessageToContent(2, totalRowsPercentage);
      addMessageToContent(3, currentTable);
      addMessageToContent(4, currentTableRowsPercentage);
    } else {
      final String retrieving = buildSimpleMessage("", messages.progressBarPanelTextForRetrievingTableStructure());
      addMessageToContent(0, retrieving);
    }

    if (progressData.isFinished()) {
      stopUpdating();
    }
  }

  @Override
  protected void onDetach() {
    stopUpdating();
    super.onDetach();
  }

  private void stopUpdating() {
    if (autoUpdateTimer != null) {
      autoUpdateTimer.cancel();
    }
  }

  private void addMessageToContent(final int index, final String message) {
    Label newMessage = new Label(message);
    final int widgetCount = content.getWidgetCount();
    if (widgetCount > 0) {
      if (index < widgetCount) {
        Label lastMessage = (Label) content.getWidget(index);
        if (!newMessage.getText().equals(lastMessage.getText())) {
          content.insert(newMessage, index);
          content.remove(index + 1);
          content.getElement().setScrollTop(content.getElement().getScrollHeight());
        }
      } else {
        content.add(newMessage);
        content.getElement().setScrollTop(content.getElement().getScrollHeight());
      }
    } else {
      content.add(newMessage);
      content.getElement().setScrollTop(content.getElement().getScrollHeight());
    }
  }

  private String buildPercentageMessage(final String type, final long processed, final long total) {
    // Examples Table: X of Y (Z%), Rows: X of Y (Z%), Rows on current table: X of Y
    // (Z%)
    StringBuilder sb = new StringBuilder();

    sb.append(type).append(" ").append(processed).append(" ").append(messages.of()).append(" ").append(total);

    float percent = 0;
    if (total > 0) {
      percent = (processed * 1.0F) / total;
    }

    sb.append(" (").append(NumberFormat.getPercentFormat().format(percent)).append(")");
    sb.append("\n\n");

    return sb.toString();
  }

  private String buildSimpleMessage(final String type, final String message) {
    // Example: Current table: <name>
    return type + " " + message + "\n\n";
  }
}
