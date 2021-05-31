package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import java.math.BigDecimal;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.models.status.formatters.Formatter;
import com.databasepreservation.common.client.models.status.formatters.NumberFormatter;
import com.databasepreservation.common.client.tools.NumberFormatUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class NumericColumnOptionsPanel extends ColumnOptionsPanel {
  private static BigDecimal DEFAULT_PREVIEW_VALUE = new BigDecimal("1234569");

  interface ColumnsOptionsPanelUiBinder extends UiBinder<Widget, NumericColumnOptionsPanel> {
  }

  private static ColumnsOptionsPanelUiBinder binder = GWT.create(ColumnsOptionsPanelUiBinder.class);

  @UiField
  ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  FlowPanel content;

  @UiField
  HTML description;

  @UiField
  CheckBox thousandsSeparatorOption;

  @UiField
  CheckBox decimalsOption;

  @UiField
  IntegerBox decimalOptionContent;

  @UiField
  CheckBox multiplierOption;

  @UiField
  IntegerBox multiplierOptionContent;

  @UiField
  CheckBox prefixOption;

  @UiField
  TextBox prefixOptionContent;

  @UiField
  CheckBox suffixOption;

  @UiField
  TextBox suffixOptionContent;

  @UiField
  HTML previewDescription;

  @UiField
  TextBox previewContent;

  public static ColumnOptionsPanel createInstance(TableStatus tableConfiguration, ColumnStatus columnConfiguration) {
    return new NumericColumnOptionsPanel(tableConfiguration, columnConfiguration);
  }

  private NumericColumnOptionsPanel(TableStatus tableConfiguration, ColumnStatus columnConfiguration) {
    initWidget(binder.createAndBindUi(this));
    description.setHTML(messages.columnManagementTextForTemplateHint(ViewerConstants.TEMPLATE_ENGINE_LINK));
    previewDescription.setHTML(SafeHtmlUtils.fromSafeConstant("Edit the value to preview a specific configuration"));

    setupPreviewContent();
    setupNumericTextBox(decimalOptionContent, multiplierOptionContent);
    setupCheckboxOptions();
    setupHandlers();
    setup(columnConfiguration);

    updatePreviewValue();
  }

  private void setupHandlers() {
    KeyUpHandler keyUpHandler = keyUpEvent -> updatePreviewValue();

    decimalOptionContent.addKeyUpHandler(keyUpHandler);
    decimalOptionContent.addClickHandler(clickEvent -> updatePreviewValue());
    multiplierOptionContent.addKeyUpHandler(keyUpHandler);
    multiplierOptionContent.addClickHandler(clickEvent -> updatePreviewValue());
    prefixOptionContent.addKeyUpHandler(keyUpHandler);
    suffixOptionContent.addKeyUpHandler(keyUpHandler);
  }

  private void setupPreviewContent() {
    previewContent.setText(String.valueOf(DEFAULT_PREVIEW_VALUE));
  }

  private void setup(ColumnStatus config) {
    if (config.getFormatter() instanceof NumberFormatter) {
      NumberFormatter formatter = (NumberFormatter) config.getFormatter();
      thousandsSeparatorOption.setValue(formatter.isThousandsSeparator());
      decimalsOption.setValue(formatter.isDecimals());
      decimalOptionContent.setValue(formatter.getDecimalPlaces());
      decimalOptionContent.setEnabled(formatter.isDecimals());
      multiplierOption.setValue(formatter.isMultiplier());
      multiplierOptionContent.setValue(formatter.getMultiplierNumber());
      multiplierOptionContent.setEnabled(formatter.isMultiplier());
      prefixOption.setValue(formatter.isPrefix());
      prefixOptionContent.setText(formatter.getPrefixContent());
      prefixOptionContent.setEnabled(formatter.isPrefix());
      suffixOption.setValue(formatter.isSuffix());
      suffixOptionContent.setText(formatter.getSuffixContent());
      suffixOptionContent.setEnabled(formatter.isSuffix());
    } else {
      decimalOptionContent.setValue(0);
      decimalOptionContent.setEnabled(false);
      multiplierOptionContent.setValue(1);
      multiplierOptionContent.setEnabled(false);
    }
  }

  private void setupNumericTextBox(Widget... widgets) {
    for (Widget widget : widgets) {
      widget.getElement().setAttribute("type", "number");
      widget.getElement().setAttribute("min", "0");
    }
  }

  private void setupCheckboxOptions() {
    thousandsSeparatorOption.addValueChangeHandler(e -> {
      updatePreviewValue();
    });
    decimalsOption.addClickHandler(e -> {
      decimalOptionContent.setEnabled(decimalsOption.getValue());
      decimalOptionContent.getElement().focus();
      updatePreviewValue();
    });
    multiplierOption.addClickHandler(e -> {
      multiplierOptionContent.setEnabled(multiplierOption.getValue());
      multiplierOptionContent.getElement().focus();
      updatePreviewValue();
    });
    prefixOption.addClickHandler(e -> {
      prefixOptionContent.setEnabled(prefixOption.getValue());
      prefixOptionContent.getElement().focus();
      updatePreviewValue();
    });
    suffixOption.addClickHandler(e -> {
      suffixOptionContent.setEnabled(suffixOption.getValue());
      suffixOptionContent.getElement().focus();
      updatePreviewValue();
    });
  }

  private void updatePreviewValue() {
    String value = DEFAULT_PREVIEW_VALUE.toString();

    if (multiplierOption.getValue() && NumberFormatUtils.isNumber(multiplierOptionContent.getText())) {
      BigDecimal multiply = DEFAULT_PREVIEW_VALUE.multiply(new BigDecimal(multiplierOptionContent.getText()));
      value = multiply.toString();
    }

    if (decimalsOption.getValue() && NumberFormatUtils.isNumber(decimalOptionContent.getText())) {
      value = NumberFormatUtils.getFormatted(new BigDecimal(value).doubleValue(), decimalOptionContent.getValue(),
        thousandsSeparatorOption.getValue());
    }

    if (thousandsSeparatorOption.getValue() && !decimalsOption.getValue()) {
      value = NumberFormat.getDecimalFormat().format(new BigDecimal(value).doubleValue());
    }

    if (suffixOption.getValue()) {
      String suffix = suffixOptionContent.getText();
      value = value.concat(suffix);
    }

    if (prefixOption.getValue()) {
      String prefix = prefixOptionContent.getText();
      value = prefix.concat(value);
    }

    previewContent.setText(value);
  }

  public Formatter getFormatter() {
    NumberFormatter numberFormatter = new NumberFormatter();
    numberFormatter.setThousandsSeparator(thousandsSeparatorOption.getValue());

    numberFormatter.setDecimals(false);
    numberFormatter.setDecimalPlaces(0);
    if (decimalsOption.getValue()) {
      numberFormatter.setDecimals(true);
      numberFormatter.setDecimalPlaces(decimalOptionContent.getValue());
    }

    numberFormatter.setMultiplier(false);
    numberFormatter.setMultiplierNumber(1);
    if (multiplierOption.getValue()) {
      numberFormatter.setMultiplier(true);
      numberFormatter.setMultiplierNumber(multiplierOptionContent.getValue());
    }

    numberFormatter.setPrefix(false);
    numberFormatter.setPrefixContent("");
    if (prefixOption.getValue()) {
      numberFormatter.setPrefix(true);
      numberFormatter.setPrefixContent(prefixOptionContent.getText());
    }

    numberFormatter.setSuffix(false);
    numberFormatter.setSuffixContent("");
    if (suffixOption.getValue()) {
      numberFormatter.setSuffix(true);
      numberFormatter.setSuffixContent(suffixOptionContent.getText());
    }

    return numberFormatter;
  }

  @Override
  public TemplateStatus getSearchTemplate() {
    return null;
  }

  @Override
  public TemplateStatus getDetailsTemplate() {
    return null;
  }

  @Override
  public TemplateStatus getExportTemplate() {
    return null;
  }
}
