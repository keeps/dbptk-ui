package com.databasepreservation.common.shared.client.common.fields;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.rabbitmq.client.AMQP;

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

  public static RowField createInstance(String iconTag, String label, Widget value, Widget more) {
    return new RowField(iconTag, label, value, more);
  }

  public static RowField createInstance(String label, Widget value) {
    return new RowField(null, label, value, null);
  }

  public static RowField createInstance(String iconTag, String label, Widget value) {
    return new RowField(iconTag, label, value, null);
  }

  private RowField(String iconTag, String label, Widget value, Widget more) {
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
        Label l = new Label(label);
        l.addStyleName("metadata-information-element-label");
        rowField.add(l);
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
    addRelatedTo(safeHtml, null);
  }

  public void addRelatedTo(SafeHtml safeHtml, String css) {
    FlowPanel panel = new FlowPanel();
    if (css != null) {
      panel.addStyleName(css);
    }
    panel.add(new HTML(safeHtml));
    main.add(panel);
  }

  public void addReferencedBy(SafeHtml safeHtml) {
    addReferencedBy(safeHtml, null);
  }

  public void addReferencedBy(SafeHtml safeHtml, String css) {
    FlowPanel panel = new FlowPanel();
    if (css != null) {
      panel.addStyleName(css);
    }
    panel.add(new HTML(safeHtml));
    main.add(panel);
  }
}