package com.databasepreservation.common.client.models.status.formatters;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@JsonTypeName("numberFormatter")
@JsonPropertyOrder({"thousandsSeparator", "decimals", "decimalPlaces", "multiplier", "multiplierNumber", "prefix",
  "prefixContent", "suffix", "suffixContent"})
public class NumberFormatter implements Formatter {

  private boolean thousandsSeparator;
  private boolean decimals;
  private boolean multiplier;
  private boolean prefix;
  private boolean suffix;

  private int decimalPlaces;
  private int multiplierNumber;
  private String prefixContent = "";
  private String suffixContent = "";

  public NumberFormatter() {
    thousandsSeparator = false;
    decimals = false;
    decimalPlaces = 0;
    multiplier = false;
    multiplierNumber = 1;
    prefix = false;
    suffix = false;
  }

  public boolean isThousandsSeparator() {
    return thousandsSeparator;
  }

  public void setThousandsSeparator(boolean thousandsSeparator) {
    this.thousandsSeparator = thousandsSeparator;
  }

  public boolean isDecimals() {
    return decimals;
  }

  public void setDecimals(boolean decimals) {
    this.decimals = decimals;
  }

  public boolean isMultiplier() {
    return multiplier;
  }

  public void setMultiplier(boolean multiplier) {
    this.multiplier = multiplier;
  }

  public boolean isPrefix() {
    return prefix;
  }

  public void setPrefix(boolean prefix) {
    this.prefix = prefix;
  }

  public boolean isSuffix() {
    return suffix;
  }

  public void setSuffix(boolean suffix) {
    this.suffix = suffix;
  }

  public int getDecimalPlaces() {
    return decimalPlaces;
  }

  public void setDecimalPlaces(int decimalPlaces) {
    this.decimalPlaces = decimalPlaces;
  }

  public int getMultiplierNumber() {
    return multiplierNumber;
  }

  public void setMultiplierNumber(int multiplierNumber) {
    this.multiplierNumber = multiplierNumber;
  }

  public String getPrefixContent() {
    return prefixContent;
  }

  public void setPrefixContent(String prefixContent) {
    this.prefixContent = prefixContent;
  }

  public String getSuffixContent() {
    return suffixContent;
  }

  public void setSuffixContent(String suffixContent) {
    this.suffixContent = suffixContent;
  }

  @Override
  public String toString() {
    return "NumberFormatter{" + "thousandsSeparator=" + thousandsSeparator + ", decimals=" + decimals + ", multiplier="
      + multiplier + ", prefix=" + prefix + ", suffix=" + suffix + ", decimalPlaces=" + decimalPlaces
      + ", multiplierNumber=" + multiplierNumber + ", prefixContent='" + prefixContent + '\'' + ", suffixContent='"
      + suffixContent + '\'' + '}';
  }
}
