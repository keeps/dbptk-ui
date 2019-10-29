package com.databasepreservation.common.shared.client.common.utils;

import java.util.List;

import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.common.shared.ViewerStructure.ViewerView;
import com.databasepreservation.common.shared.client.common.MetadataField;
import com.databasepreservation.common.shared.client.common.desktop.GenericField;
import com.databasepreservation.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.common.shared.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class CommonClientUtils {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static void addSchemaInfoToFlowPanel(FlowPanel panel, ViewerSchema schema) {
    MetadataField schemaName = MetadataField.createInstance(messages.schemaName(), schema.getName());
    schemaName.setCSSMetadata("metadata-field", "metadata-information-element-label",
        "metadata-information-element-value");

    panel.add(schemaName);
    if (ViewerStringUtils.isNotBlank(schema.getDescription())) {
      MetadataField description = MetadataField.createInstance(messages.schemaDescription(), schema.getDescription());
      description.setCSSMetadata("metadata-field", "metadata-information-element-label",
          "metadata-information-element-value");
      panel.add(description);
    }
  }

  public static FlowPanel getPanelInformation(String label, String text, String classes) {
    FlowPanel panel = new FlowPanel();

    SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
    safeHtmlBuilder.append(SafeHtmlUtils.fromString(text));

    HTML html = new HTML(safeHtmlBuilder.toSafeHtml());
    html.addStyleName(classes);

    GenericField genericField = GenericField.createInstance(label, html);
    genericField.setCSSMetadata("metadata-field", "metadata-information-element-label");
    panel.add(genericField);

    return panel;
  }

  public static FlowPanel getCardTitle(String text) {
    FlowPanel panel = new FlowPanel();
    SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
    safeHtmlBuilder.append(SafeHtmlUtils.fromString(text));
    HTML html = new HTML(safeHtmlBuilder.toSafeHtml());
    panel.add(html);

    return panel;
  }

  public static FlowPanel getHeader(String iconTag, ViewerTable table, String hClass) {
    String displayName;
    if (table.getName().startsWith(ViewerConstants.MATERIALIZED_VIEW_PREFIX)) {
      displayName = table.getName().substring(ViewerConstants.MATERIALIZED_VIEW_PREFIX.length());
    } else {
      displayName = table.getName();
    }

    return getHeader(iconTag, displayName, hClass);
  }

  public static FlowPanel getHeader(String iconTag, String title, String hClass) {
    FlowPanel panel = new FlowPanel();

    SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
    safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant(iconTag)).appendEscaped(" ")
        .append(SafeHtmlUtils.fromString(title));

    HTML html = new HTML(safeHtmlBuilder.toSafeHtml());
    html.addStyleName(hClass);
    panel.add(html);

    return panel;
  }

  public static FlowPanel getHeader(String iconTag, SafeHtml title, String hClass) {
    FlowPanel panel = new FlowPanel();

    SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
    safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant(iconTag)).appendEscaped(" ")
        .append(title);

    HTML html = new HTML(safeHtmlBuilder.toSafeHtml());
    html.addStyleName(hClass);
    panel.add(html);

    return panel;
  }

  public static Anchor getAnchorForLOBDownload(final String databaseUUID, final String tableUUID, final String rowUUID,
    final int columnIndexInEnclosingTable) {
    StringBuilder urlBuilder = new StringBuilder();
    String base = com.google.gwt.core.client.GWT.getHostPageBaseURL();
    String servlet = ViewerConstants.API_SERVLET;
    String resource = ViewerConstants.API_V1_LOBS_RESOURCE;
    urlBuilder.append(servlet).append(resource).append("/").append(databaseUUID).append("/").append(tableUUID)
      .append("/").append(rowUUID).append("/").append(columnIndexInEnclosingTable);
    return new Anchor(messages.row_downloadLOB(), urlBuilder.toString());
  }

  public static SafeHtmlBuilder constructViewQuery(ViewerView view) {
    SafeHtmlBuilder infoBuilder = new SafeHtmlBuilder();

    if (ViewerStringUtils.isNotBlank(view.getQueryOriginal())) {
      infoBuilder.append(CommonClientUtils.getFieldHTML(messages.originalQuery(), SafeHtmlUtils.fromSafeConstant(
        "<pre><code class='sql'>" + SafeHtmlUtils.htmlEscape(view.getQueryOriginal()) + "</code></pre>")));
    }

    if (ViewerStringUtils.isNotBlank(view.getQuery())) {
      infoBuilder.append(CommonClientUtils.getFieldHTML(messages.query(), SafeHtmlUtils
        .fromSafeConstant("<pre><code class='sql'>" + SafeHtmlUtils.htmlEscape(view.getQuery()) + "</code></pre>")));
    }

    return infoBuilder;
  }

  public static SafeHtmlBuilder constructSpan(String value, String title, String css) {
    SafeHtmlBuilder span = new SafeHtmlBuilder();

    if (title == null) title = "";

    span.append(SafeHtmlUtils.fromSafeConstant("<span class='"))
            .append(SafeHtmlUtils.fromSafeConstant(css))
            .append(SafeHtmlUtils.fromSafeConstant("' title='"))
            .append(SafeHtmlUtils.fromSafeConstant(title))
            .append(SafeHtmlUtils.fromSafeConstant("'>"))
            .append(SafeHtmlUtils.fromSafeConstant(value))
            .append(SafeHtmlUtils.fromSafeConstant("</span>"));
    return span;
  }

  public static SafeHtml wrapOnDiv(List<SafeHtmlBuilder> builders) {
    SafeHtmlBuilder div = new SafeHtmlBuilder();

    div.append(SafeHtmlUtils.fromSafeConstant("<div>"));
    for (SafeHtmlBuilder builder : builders) {
      div.append(builder.toSafeHtml());
    }
    div.append(SafeHtmlUtils.fromSafeConstant("</div>"));

    return div.toSafeHtml();
  }

  public static FlowPanel getSchemaAndTableHeader(String databaseUUID, ViewerTable table, String hClass) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("schema-table-header");

    // add icon
    String iconTag = FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE);
    HTML html = new HTML(SafeHtmlUtils.fromSafeConstant(iconTag));
    html.addStyleName(hClass);
    panel.add(html);

    // add link schema
    Hyperlink schemaLink;
    switch (ApplicationType.getType()) {
      case ViewerConstants.DESKTOP:
        schemaLink = new Hyperlink(table.getSchemaName(),
          HistoryManager.linkToDesktopSchema(databaseUUID, table.getSchemaUUID()));
        schemaLink.addStyleName(hClass);
        panel.add(schemaLink);
        break;
      case ViewerConstants.SERVER:
      default:
        schemaLink = new Hyperlink(table.getSchemaName(),
          HistoryManager.linkToSchema(databaseUUID, table.getSchemaUUID()));
        schemaLink.addStyleName(hClass);
        panel.add(schemaLink);
    }

    // add /
    // Label slashSeparator = new Label("/");
    // slashSeparator.addStyleName(hClass);
    // panel.add(slashSeparator);

    iconTag = FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_TABLE_SEPARATOR);
    html = new HTML(SafeHtmlUtils.fromSafeConstant(iconTag));
    html.addStyleName(hClass);
    panel.add(html);

    // add link table
    Hyperlink tableLink;
    switch (ApplicationType.getType()) {
      case ViewerConstants.DESKTOP:
        tableLink = new Hyperlink(table.getName(), HistoryManager.linkToDesktopTable(databaseUUID, table.getUUID()));
        tableLink.addStyleName(hClass);
        panel.add(tableLink);
        break;
      case ViewerConstants.SERVER:
      default:
        tableLink = new Hyperlink(table.getName(), HistoryManager.linkToTable(databaseUUID, table.getUUID()));
        tableLink.addStyleName(hClass);
        panel.add(tableLink);
    }

    return panel;
  }

  public static FlowPanel getSavedSearchHeader(String databaseUUID, String savedSearchName) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("schema-table-header");
    String hClass = "h1";

    // add icon
    String iconTag = FontAwesomeIconManager.getTag(FontAwesomeIconManager.SAVED_SEARCH);
    HTML html = new HTML(SafeHtmlUtils.fromSafeConstant(iconTag));
    html.addStyleName(hClass);
    panel.add(html);

    // add view name
    Label savedSearchLabel = new Label(savedSearchName);
    savedSearchLabel.addStyleName(hClass);
    panel.add(savedSearchLabel);

    return panel;
  }

  public static FlowPanel getSchemaAndViewHeader(String databaseUUID, ViewerSchema schema, ViewerView view,
    String hClass) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("schema-table-header");

    // add icon
    String iconTag = FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_VIEWS);
    HTML html = new HTML(SafeHtmlUtils.fromSafeConstant(iconTag));
    html.addStyleName(hClass);
    panel.add(html);

    // add link schema
    Hyperlink schemaLink;
    switch (ApplicationType.getType()) {
      case ViewerConstants.DESKTOP:
        schemaLink = new Hyperlink(schema.getName(), HistoryManager.linkToDesktopSchema(databaseUUID, schema.getUUID()));
        schemaLink.addStyleName(hClass);
        panel.add(schemaLink);
        break;
      case ViewerConstants.SERVER:
      default:
        schemaLink = new Hyperlink(schema.getName(), HistoryManager.linkToSchema(databaseUUID, schema.getUUID()));
        schemaLink.addStyleName(hClass);
        panel.add(schemaLink);
    }

    iconTag = FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_TABLE_SEPARATOR);
    html = new HTML(SafeHtmlUtils.fromSafeConstant(iconTag));
    html.addStyleName(hClass);
    panel.add(html);

    // add view name
    Label viewLink = new Label(view.getName());
    viewLink.addStyleName(hClass);
    panel.add(viewLink);

    return panel;
  }

  public static SafeHtml getFieldHTML(String label, String value) {
    boolean blankLabel = ViewerStringUtils.isBlank(label);
    boolean blankValue = ViewerStringUtils.isBlank(value);

    if (blankLabel || blankValue) {
      return SafeHtmlUtils.EMPTY_SAFE_HTML;

    } else {
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"field\">"));
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"label\">"));
      b.append(SafeHtmlUtils.fromString(label));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"value\">"));
      b.append(SafeHtmlUtils.fromString(value));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
      return b.toSafeHtml();
    }
  }

  public static SafeHtml getFieldHTML(String label, SafeHtml value) {
    boolean blankLabel = ViewerStringUtils.isBlank(label);
    boolean blankValue = value == null || value == SafeHtmlUtils.EMPTY_SAFE_HTML;

    if (blankLabel || blankValue) {
      return SafeHtmlUtils.EMPTY_SAFE_HTML;

    } else {
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"field\">"));
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"label\">"));
      b.append(SafeHtmlUtils.fromString(label));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"value\">"));
      b.append(value);
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
      b.append(SafeHtmlUtils.fromSafeConstant("</div>"));
      return b.toSafeHtml();
    }
  }
}
