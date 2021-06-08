package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import java.math.BigDecimal;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.status.collection.ColumnStatus;
import com.databasepreservation.common.client.models.status.collection.TableStatus;
import com.databasepreservation.common.client.models.status.collection.TemplateStatus;
import com.databasepreservation.common.client.models.status.formatters.Formatter;
import com.databasepreservation.common.client.models.status.formatters.NumberFormatter;
import com.databasepreservation.common.client.tools.NumberFormatUtils;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
  private static final BigDecimal DEFAULT_PREVIEW_VALUE = new BigDecimal("3453.34");

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

  private String customValue = "";

  public static ColumnOptionsPanel createInstance(ColumnStatus columnConfiguration) {
    return new NumericColumnOptionsPanel(columnConfiguration);
  }

  private NumericColumnOptionsPanel(ColumnStatus columnConfiguration) {
    initWidget(binder.createAndBindUi(this));
    description.setHTML(messages.columnManagementNumericFormatterTextForDescription());
    previewDescription.setHTML(messages.columnManagementNumericFormatterTextForPreviewDescription());

    setupPreviewContent();
    setupNumericTextBox(decimalOptionContent, multiplierOptionContent);
    setupCheckboxOptions();
    setupHandlers();
    setup(columnConfiguration);

    updatePreviewValue();
  }

  private void setupHandlers() {
    KeyUpHandler keyUpHandler = keyUpEvent -> updatePreviewValue();
    ClickHandler clickHandler = ClickHandler -> updatePreviewValue();

    decimalOptionContent.addKeyUpHandler(keyUpHandler);
    decimalOptionContent.addClickHandler(clickHandler);
    multiplierOptionContent.addKeyUpHandler(keyUpHandler);
    multiplierOptionContent.addClickHandler(clickHandler);
    prefixOptionContent.addKeyUpHandler(keyUpHandler);
    suffixOptionContent.addKeyUpHandler(keyUpHandler);
  }

  private void setupPreviewContent() {
    previewContent.setText(String.valueOf(DEFAULT_PREVIEW_VALUE));

    previewContent.addValueChangeHandler(e -> {
      if (ViewerStringUtils.isBlank(previewContent.getText())) {
        customValue = "";
      } else {
        if (NumberFormatUtils.isNumber(previewContent.getText())) {
          customValue = previewContent.getText();
        } else {
          customValue = "";
        }
      }

      updatePreviewValue();
    });
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

    if (ViewerStringUtils.isNotBlank(customValue)) {
      value = customValue;
    }

    if (validate()) {
      NumberFormatter numberFormatter = (NumberFormatter) getFormatter();
      String result = NumberFormatUtils.getFormattedValue(numberFormatter, value);

      previewContent.setText(result);
    }
  }

  public boolean validate() {
    if (!decimalsOption.getValue() && !multiplierOption.getValue()) {
      return true;
    }

    String decimalsPlace = decimalOptionContent.getText();
    String multiplier = multiplierOptionContent.getText();

    if (decimalsOption.getValue() && !multiplierOption.getValue()) {
      return NumberFormatUtils.isNumber(decimalsPlace);
    }

    if (!decimalsOption.getValue() && multiplierOption.getValue()) {
      return NumberFormatUtils.isNumber(multiplier);
    }

    return NumberFormatUtils.isNumber(decimalsPlace) && NumberFormatUtils.isNumber(multiplier);
  }

  public Formatter getFormatter() {
    NumberFormatter numberFormatter = new NumberFormatter();
    numberFormatter.setThousandsSeparator(thousandsSeparatorOption.getValue());

    if (decimalsOption.getValue()) {
      numberFormatter.setDecimals(true);
      numberFormatter.setDecimalPlaces(decimalOptionContent.getValue());
    }

    if (multiplierOption.getValue()) {
      numberFormatter.setMultiplier(true);
      numberFormatter.setMultiplierNumber(multiplierOptionContent.getValue());
    }

    if (prefixOption.getValue()) {
      numberFormatter.setPrefix(true);
      numberFormatter.setPrefixContent(prefixOptionContent.getText());
    }

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
