package com.databasepreservation.common.client.common.utils;

import java.util.List;

import com.databasepreservation.common.client.common.fields.GenericField;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.databasepreservation.common.client.models.structure.ViewerView;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;
import org.intellij.lang.annotations.Flow;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class CommonClientUtils {
	private static final ClientMessages messages = GWT.create(ClientMessages.class);
	private static final String CLOSE_DIV = "</div>";

	private CommonClientUtils() {}

	public static FlowPanel getAdvancedSearchDivider(final String label) {
		FlowPanel panel = new FlowPanel();
		panel.addStyleName("divider");

		FlowPanel dividerHeader = new FlowPanel();
		dividerHeader.addStyleName("divider-header");
		dividerHeader.add(new Label(label));

		FlowPanel dividerLine = new FlowPanel();
		dividerLine.addStyleName("divider-line");
		Element el = DOM.createSpan();
		dividerLine.getElement().appendChild(el);

		panel.add(dividerHeader);
		panel.add(dividerLine);

		return panel;
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

  public static HTML getHeader(ViewerTable table, String hClass, boolean multiSchema) {
    if (multiSchema) {
      return getHeaderMultiSchema(table, hClass);
    } else {
      return getHeaderSingleSchema(table, hClass);
    }
  }

  public static HTML getHeader(TableStatus tableStatus, ViewerTable table, String hClass, boolean multiSchema) {
    if (multiSchema) {
      return getHeaderMultiSchema(tableStatus, table, hClass);
    } else {
      return getHeaderSingleSchema(tableStatus, table, hClass);
    }
  }

  private static HTML getHeaderMultiSchema(TableStatus status, ViewerTable table, String hClass) {
    String separatorIconTag = FontAwesomeIconManager.getTagWithStyleName(FontAwesomeIconManager.SCHEMA_TABLE_SEPARATOR,
      "schema-table-separator");

    if (table.isCustomView()) {
      final SafeHtml stackedIconSafeHtml = FontAwesomeIconManager.getStackedIconSafeHtml(
        FontAwesomeIconManager.SCHEMA_VIEWS, FontAwesomeIconManager.COG,
        table.getSchemaName() + " " + separatorIconTag + " " + status.getCustomName());
      return getHeader(stackedIconSafeHtml, hClass);
    } else if (table.isMaterializedView()) {
      final SafeHtml stackedIconSafeHtml = FontAwesomeIconManager.getStackedIconSafeHtml(
        FontAwesomeIconManager.SCHEMA_VIEWS, FontAwesomeIconManager.TABLE,
        table.getSchemaName() + " " + separatorIconTag + " " + status.getCustomName());
      return getHeader(stackedIconSafeHtml, hClass);
    } else {
      final SafeHtml tagSafeHtml = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE,
        table.getSchemaName() + " " + separatorIconTag + " " + status.getCustomName());
      return getHeader(tagSafeHtml, hClass);
    }
  }

  private static HTML getHeaderMultiSchema(ViewerTable table, String hClass) {
    String separatorIconTag = FontAwesomeIconManager.getTagWithStyleName(FontAwesomeIconManager.SCHEMA_TABLE_SEPARATOR,
      "schema-table-separator");

    if (table.isCustomView()) {
      final SafeHtml stackedIconSafeHtml = FontAwesomeIconManager.getStackedIconSafeHtml(
        FontAwesomeIconManager.SCHEMA_VIEWS, FontAwesomeIconManager.COG,
        table.getSchemaName() + " " + separatorIconTag + " " + table.getNameWithoutPrefix());
      return getHeader(stackedIconSafeHtml, hClass);
    } else if (table.isMaterializedView()) {
      final SafeHtml stackedIconSafeHtml = FontAwesomeIconManager.getStackedIconSafeHtml(
        FontAwesomeIconManager.SCHEMA_VIEWS, FontAwesomeIconManager.TABLE,
        table.getSchemaName() + " " + separatorIconTag + " " + table.getNameWithoutPrefix());
      return getHeader(stackedIconSafeHtml, hClass);
    } else {
      final SafeHtml tagSafeHtml = FontAwesomeIconManager.getTagSafeHtml(FontAwesomeIconManager.TABLE,
        table.getSchemaName() + " " + separatorIconTag + " " + table.getNameWithoutPrefix());
      return getHeader(tagSafeHtml, hClass);
    }
  }

  private static HTML getHeaderSingleSchema(ViewerTable table, String hClass) {
    if (table.isCustomView()) {
      final SafeHtml stackedIconSafeHtml = FontAwesomeIconManager.getStackedIconSafeHtml(
        FontAwesomeIconManager.SCHEMA_VIEWS, FontAwesomeIconManager.COG, table.getNameWithoutPrefix());
      return getHeader(stackedIconSafeHtml, hClass);
    } else if (table.isMaterializedView()) {
      final SafeHtml stackedIconSafeHtml = FontAwesomeIconManager.getStackedIconSafeHtml(
        FontAwesomeIconManager.SCHEMA_VIEWS, FontAwesomeIconManager.TABLE, table.getNameWithoutPrefix());
      return getHeader(stackedIconSafeHtml, hClass);
    } else {
      final String tag = FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE);
      return getHeader(tag, table, hClass);
    }
  }

  private static HTML getHeaderSingleSchema(TableStatus status, ViewerTable table, String hClass) {
    if (table.isCustomView()) {
      final SafeHtml stackedIconSafeHtml = FontAwesomeIconManager.getStackedIconSafeHtml(
        FontAwesomeIconManager.SCHEMA_VIEWS, FontAwesomeIconManager.COG, status.getCustomName());
      return getHeader(stackedIconSafeHtml, hClass);
    } else if (table.isMaterializedView()) {
      final SafeHtml stackedIconSafeHtml = FontAwesomeIconManager.getStackedIconSafeHtml(
        FontAwesomeIconManager.SCHEMA_VIEWS, FontAwesomeIconManager.TABLE, status.getCustomName());
      return getHeader(stackedIconSafeHtml, hClass);
    } else {
      final String tag = FontAwesomeIconManager.getTag(FontAwesomeIconManager.TABLE);
      return getHeader(tag, status.getCustomName(), hClass);
    }
  }

  public static HTML getHeader(SafeHtml iconStack, String hClass) {
    HTML html = new HTML(iconStack);
    html.addStyleName(hClass);

    return html;
  }

  private static HTML getHeader(String iconTag, ViewerTable table, String hClass) {
    return getHeader(iconTag, table.getNameWithoutPrefix(), hClass);
  }

  public static HTML getHeaderHTML(String iconTag, String title, String styleName) {
    SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
    safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant(iconTag)).appendEscaped(" ")
      .append(SafeHtmlUtils.fromString(title));

    HTML html = new HTML(safeHtmlBuilder.toSafeHtml());
    html.addStyleName(styleName);

    return html;
  }

  public static HTML getHeader(String iconTag, String title, String hClass) {
		SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
    safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant(iconTag)).appendEscaped(" ")
      .append(SafeHtmlUtils.fromString(title));

    HTML html = new HTML(safeHtmlBuilder.toSafeHtml());
    html.addStyleName(hClass);

    return html;
  }

  public static FlowPanel getHeader(String iconTag, SafeHtml title, String hClass) {
    FlowPanel panel = new FlowPanel();

    SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
    safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant(iconTag)).appendEscaped(" ").append(title);

    HTML html = new HTML(safeHtmlBuilder.toSafeHtml());
    html.addStyleName(hClass);
    panel.add(html);

    return panel;
  }

  public static Anchor wrapOnAnchor(String message, String uri) {
    return new Anchor(message, uri);
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

    if (title == null)
      title = "";

    span.append(SafeHtmlUtils.fromSafeConstant("<span class='")).append(SafeHtmlUtils.fromSafeConstant(css))
      .append(SafeHtmlUtils.fromSafeConstant("' title='")).append(SafeHtmlUtils.fromSafeConstant(title))
      .append(SafeHtmlUtils.fromSafeConstant("'>")).append(SafeHtmlUtils.fromSafeConstant(value))
      .append(SafeHtmlUtils.fromSafeConstant("</span>"));
    return span;
  }

  public static SafeHtml wrapOnDiv(List<SafeHtmlBuilder> builders) {
    SafeHtmlBuilder div = new SafeHtmlBuilder();

    div.append(SafeHtmlUtils.fromSafeConstant("<div>"));
    for (SafeHtmlBuilder builder : builders) {
      div.append(builder.toSafeHtml());
    }
    div.append(SafeHtmlUtils.fromSafeConstant(CLOSE_DIV));

    return div.toSafeHtml();
  }

  public static FlowPanel wrapOnDiv(String divClassName, Widget... widgets) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName(divClassName);
    for (Widget widget : widgets) {
      panel.add(widget);
    }

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
      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_DIV));
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"value\">"));
      b.append(SafeHtmlUtils.fromString(value));
      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_DIV));
      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_DIV));
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
      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_DIV));
      b.append(SafeHtmlUtils.fromSafeConstant("<div class=\"value\">"));
      b.append(value);
      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_DIV));
      b.append(SafeHtmlUtils.fromSafeConstant(CLOSE_DIV));
      return b.toSafeHtml();
    }
  }
}
