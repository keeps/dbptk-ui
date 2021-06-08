package com.databasepreservation.common.client.tools;

import java.math.BigDecimal;

import com.databasepreservation.common.client.models.status.formatters.NumberFormatter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class NumberFormatUtils {

  public static boolean isNumber(String text) {
    if (ViewerStringUtils.isBlank(text)) {
      return false;
    }

    try {
      new BigDecimal(text);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  public static String getFormatted(BigDecimal value, int decimalCount, boolean thousandsSeparator) {
    String pattern = NumberFormat.getDecimalFormat().getPattern();

    String defaultDecimalPart = "0.";

    if (thousandsSeparator) {
      defaultDecimalPart = ".";
    }

    StringBuilder numberPattern = new StringBuilder((decimalCount <= 0) ? "" : defaultDecimalPart);
    for (int i = 0; i < decimalCount; i++) {
      numberPattern.append('0');
    }

    if (thousandsSeparator) {
      String s = pattern.replaceAll(".###", numberPattern.toString());
      return NumberFormat.getFormat(s).format(value);
    }

    return NumberFormat.getFormat(numberPattern.toString()).format(value);
  }

  public static String getFormattedValue(NumberFormatter numberFormatter, String value) {
    String result = value;
    if (numberFormatter.isMultiplier()) {
      result = new BigDecimal(value).multiply(new BigDecimal(numberFormatter.getMultiplierNumber())).toString();
    }

    if (numberFormatter.isDecimals()) {
      result = NumberFormatUtils.getFormatted(new BigDecimal(result), numberFormatter.getDecimalPlaces(),
        numberFormatter.isThousandsSeparator());
    }

    if (numberFormatter.isThousandsSeparator() && !numberFormatter.isDecimals()) {
      result = NumberFormat.getDecimalFormat().format(new BigDecimal(result).doubleValue());
    }

    if (numberFormatter.isSuffix()) {
      String suffix = numberFormatter.getSuffixContent();
      result = result.concat(suffix);
    }

    if (numberFormatter.isPrefix()) {
      String prefix = numberFormatter.getPrefixContent();
      result = prefix.concat(result);
    }

    return result;
  }
}
