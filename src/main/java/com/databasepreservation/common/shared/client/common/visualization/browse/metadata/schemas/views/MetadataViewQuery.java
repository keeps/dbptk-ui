package com.databasepreservation.common.shared.client.common.visualization.browse.metadata.schemas.views;

import com.databasepreservation.common.shared.ViewerStructure.ViewerView;
import com.databasepreservation.common.shared.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.shared.client.common.utils.JavascriptUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataViewQuery {

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private final ViewerView view;

  public MetadataViewQuery(ViewerView view) {
    this.view = view;
  }

  public ScrollPanel createInfo() {

    FlowPanel flowPanel = new FlowPanel();

    if (view.getQuery() == null || view.getQuery().isEmpty()) {
      flowPanel
        .add(new HTMLPanel(CommonClientUtils.getFieldHTML(messages.query(), messages.viewDoesNotContainQuery())));
    } else {
      flowPanel.add(new HTMLPanel(CommonClientUtils.getFieldHTML(messages.query(), view.getQuery())));
    }

    if (view.getQueryOriginal() == null || view.getQueryOriginal().isEmpty()) {
      flowPanel.add(new HTMLPanel(
        CommonClientUtils.getFieldHTML(messages.originalQuery(), messages.viewDoesNotContainQueryOriginal())));
    } else {
      flowPanel.add(new HTMLPanel(CommonClientUtils.getFieldHTML(messages.originalQuery(), SafeHtmlUtils
        .fromSafeConstant("<pre><code>" + SafeHtmlUtils.htmlEscape(view.getQueryOriginal()) + "</code></pre>"))));
    }

    JavascriptUtils.runHighlighter(flowPanel.getElement());
    ScrollPanel panel = new ScrollPanel(flowPanel);
    panel.setSize("100%", "100%");

    return panel;
  }
}
