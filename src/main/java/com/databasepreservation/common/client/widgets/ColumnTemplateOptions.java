package com.databasepreservation.common.client.widgets;

import static com.databasepreservation.common.client.ViewerConstants.DEFAULT_DOWNLOAD_LABEL_TEMPLATE;

import com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers.ColumnOptionUtils;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ColumnTemplateOptions extends FlowPanel {

  private final Label label = new Label();
  private final TextBox template = new TextBox();
  private final HTML reset = new HTML();
  private FlowPanel displayHint = new FlowPanel();

  public ColumnTemplateOptions(String labelText, String templateText, boolean buildWithResetButton) {
    this.label.setText(labelText);
    this.template.setText(templateText);

    add(label);
    if (buildWithResetButton) {
      add(buildTextBoxWithResetButton());
    } else {
      add(template);
    }
    add(displayHint);
  }

  private FlowPanel buildTextBoxWithResetButton() {
    FlowPanel panel = new FlowPanel();
    panel.getElement().getStyle().setDisplay(Style.Display.FLEX);

    reset.getElement().getStyle().setPaddingLeft(5, Style.Unit.PX);
    reset.getElement().getStyle().setCursor(Style.Cursor.POINTER);

    String refresh = FontAwesomeIconManager.getTag("redo");
    SafeHtml safeHtml = SafeHtmlUtils.fromSafeConstant(refresh);
    reset.setHTML(safeHtml);

    reset.addClickHandler(clickEvent -> {
      template.setText(DEFAULT_DOWNLOAD_LABEL_TEMPLATE);
    });

    panel.add(template);
    panel.add(reset);

    return panel;
  }

  public void addStyleNameToLabel(String styleName) {
    this.label.addStyleName(styleName);
  }

  public void addStyleNameToTextBox(String styleName) {
    this.template.addStyleName(styleName);
  }

  public void buildHintForLabel(String i18nText) {
    displayHint.add(ColumnOptionUtils.buildHintForLabel(template, i18nText));
  }

  public void buildHintForButtons(TableStatus table, String i18nText) {
    displayHint.add(ColumnOptionUtils.buildHintWithButtons(table, template, i18nText));
  }

  public String getText() {
    return this.template.getText();
  }
}
