package com.databasepreservation.main.desktop.client.dbptk.wizard.common.progressBar;

import java.util.HashMap;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ProgressData;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.Progressbar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class ProgressBarPanel extends Composite {
  interface ProgressBarPanelUiBinder extends UiBinder<Widget, ProgressBarPanel> { }

  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  protected static HashMap<String, ProgressBarPanel> instances = new HashMap<>();
  protected String databaseUUID;
  protected static ProgressBarPanelUiBinder uiBinder = GWT.create(ProgressBarPanelUiBinder.class);

  @UiField
  FlowPanel content;

  @UiField
  Progressbar progressBar;

  @UiField
  Label title, subTitle;

  public void setTitleText(String text) {
    title.setText(text);
  }

  public void setSubtitleText(String text) {
    subTitle.setText(text);
  }

  public void clear(String uuid) {
    progressBar.setCurrent(0);
    content.clear();
    instances.put(uuid, null);
  }


  protected void addMessageToContent(final int index, final String message) {
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

  protected String buildPercentageMessage(final String type, final long processed, final long total) {
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

  protected String buildSimpleMessage(final String type, final String message) {
    // Example: Current table: <name>
    return type + " " + message + "\n\n";
  }
}
