/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.fields;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class RowField extends Composite {
  interface RowFieldUiBinder extends UiBinder<Widget, RowField> {
  }

  private static RowFieldUiBinder binder = GWT.create(RowFieldUiBinder.class);

  @UiField
  FlowPanel main;

  @UiField
  FlowPanel rowField;

  public static RowField createInstance(String iconTag, String label, String url, Widget value, Widget more) {
    return new RowField(iconTag, label, url, value, more);
  }

  public static RowField createInstance(String iconTag, String label, Widget value, Widget more) {
    return new RowField(iconTag, label, null, value, more);
  }

  public static RowField createInstance(String label, Widget value) {
    return new RowField(null, label, null, value, null);
  }

  public static RowField createInstance(String iconTag, String label, Widget value) {
    return new RowField(iconTag, label, null, value, null);
  }

  private RowField(String iconTag, String label, String url, Widget value, Widget more) {
    initWidget(binder.createAndBindUi(this));

    if (iconTag != null) {
      HTML icon = new HTML();
      SafeHtmlBuilder builder = new SafeHtmlBuilder();
      builder.append(SafeHtmlUtils.fromSafeConstant(iconTag)).appendEscaped(" ")
        .append(SafeHtmlUtils.fromSafeConstant(label));
      icon.setHTML(builder.toSafeHtml());
      icon.addStyleName("metadata-information-element-label");
      rowField.add(icon);
    } else {
      if (label != null) {
        if (url != null) {
          Anchor a = new Anchor(label, url);
          a.addStyleName("metadata-information-element-label");
          a.setTarget("_blank");
          rowField.add(a);
        } else {
          Label l = new Label(label);
          l.addStyleName("metadata-information-element-label");
          rowField.add(l);
        }
      }
    }

    if (value != null) {
      value.addStyleName("metadata-information-element-value");
      rowField.add(value);
    }

    if (more != null) {
      rowField.add(more);
    }
  }

  public void addColumnDescription(String text) {
    SimplePanel description = new SimplePanel();
    description.addStyleName("metadata-information-element-value text-muted");
    final HTML html = new HTML(text);
    html.addStyleName("column-description");
    description.setWidget(html);
    rowField.add(description);
  }

  public void addRelatedTo(SafeHtml safeHtml) {
    addRelatedTo(safeHtml, null, null);
  }

  public void addRelatedTo(SafeHtml safeHtml, String tooltip, String css) {
    FlowPanel panel = new FlowPanel();
    if (css != null) {
      panel.addStyleName(css);
    }
    panel.add(new HTML(safeHtml));
    panel.setTitle(tooltip);
    main.add(panel);
  }

  public void addReferencedBy(SafeHtml safeHtml) {
    addReferencedBy(safeHtml, null, null);
  }

  public void addReferencedBy(SafeHtml safeHtml, String tooltip, String css) {
    FlowPanel panel = new FlowPanel();
    if (css != null) {
      panel.addStyleName(css);
    }
    panel.add(new HTML(safeHtml));
    panel.setTitle(tooltip);
    main.add(panel);
  }
}