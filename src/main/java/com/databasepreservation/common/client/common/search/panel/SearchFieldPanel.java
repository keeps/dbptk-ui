/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.search.panel;

import java.util.Date;

import com.databasepreservation.common.client.ClientConfigurationManager;
import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.tractionsoftware.gwt.user.client.ui.UTCDateBox;
import com.tractionsoftware.gwt.user.client.ui.UTCTimeBox;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public abstract class SearchFieldPanel extends Composite {
  protected static final ClientMessages messages = GWT.create(ClientMessages.class);
  protected static final String LABEL = "label";
  protected static final String FULL_WIDTH = "full_width";
  protected static final String FORM_TEXTBOX_FORM_TEXTBOX_SMALL = "form-textbox form-textbox-small";
  protected static final String TRUE = "true";
  protected static final String FALSE = "false";

  protected FlowPanel inputPanel;
  private final SearchField searchField;

  public SearchFieldPanel(SearchField searchField) {
    this.searchField = searchField;
    FlowPanel panel = new FlowPanel();
    FlowPanel leftPanel = new FlowPanel();
    Label fieldLabel = new Label(searchField.getLabel());
    this.inputPanel = new FlowPanel();

    panel.add(leftPanel);

    initWidget(panel);

    panel.addStyleName("search-field");
    leftPanel.addStyleName("search-field-left-panel");
    fieldLabel.addStyleName("search-field-label");

    inputPanel.addStyleName("search-field-input-panel");
    inputPanel.addStyleName(FULL_WIDTH);

    leftPanel.add(fieldLabel);
    leftPanel.add(inputPanel);
  }

  public SearchField getSearchField() {
    return this.searchField;
  }

  protected Date getDateFromInput(UTCDateBox dateBox) {
    return new Date(dateBox.getValue());
  }

  protected Date getDateFromInput(UTCTimeBox timeBox) {
    return new Date(timeBox.getValue());
  }

  protected Date getDateFromInput(UTCDateBox dateBox, UTCTimeBox timeBox) {
    boolean isUTC = ClientConfigurationManager.getBoolean(false, "ui.interface.show.datetime.utc");
    if (isUTC) {
      return new Date(dateBox.getValue() + timeBox.getValue());
    } else {
      DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-ddTHH:mm:ss");
      return format.parse(dateBox.getText() + "T" + timeBox.getText());
    }
  }

  protected boolean dateIntervalValid(UTCDateBox inputFrom, UTCDateBox inputTo) {
    if (inputFrom.getValue() != null && inputTo.getValue() != null) {
      return inputFrom.getValue() <= inputTo.getValue();
    }

    return true;
  }

  protected boolean dateIntervalValid(UTCDateBox dateFrom, UTCTimeBox timeFrom, UTCDateBox dateTo, UTCTimeBox timeTo) {
    if (dateFrom.getValue() != null && dateTo.getValue() != null) {
      if (timeFrom.getValue() != null && timeTo.getValue() != null) {
        return dateFrom.getValue() + timeFrom.getValue() <= dateTo.getValue() + timeTo.getValue();
      } else {
        return dateFrom.getValue() <= dateTo.getValue();
      }
    }

    return true;
  }

  protected boolean dateIntervalValid(UTCTimeBox inputFrom, UTCTimeBox inputTo) {
    return inputFrom.getValue() != null && inputTo.getValue() != null && inputFrom.getValue() <= inputTo.getValue();
  }

  protected boolean valid(TextBox input) {
    return (!input.getValue().isEmpty());
  }

  protected boolean valid(ListBox input) {
    return (input.getSelectedValue().equals(TRUE) || input.getSelectedValue().equals(FALSE));
  }

  /**
   * Removes the date part (year, month, day) from a Date that contains date and
   * time (keeping whichever timezone is on the passed Date)
   */
  protected long extractTimePartWithTimeZone(Date dateAndTime, UTCDateBox sameDateInDateBox) {
    // sameDateInDateBox.getValue returns UTC time, subtracting them obtains a
    // time with timezone.
    return dateAndTime.getTime() - sameDateInDateBox.getValue();
  }

  protected boolean intervalValid(TextBox inputFrom, TextBox inputTo) {
    boolean valid = false;

    try {
      if (!inputFrom.getValue().isEmpty() && !inputTo.getValue().isEmpty()) {
        Double.parseDouble(inputFrom.getValue());
        Double.parseDouble(inputTo.getValue());
        valid = true;
      } else if (!inputFrom.getValue().isEmpty()) {
        Double.parseDouble(inputFrom.getValue());
        valid = true;
      } else if (!inputTo.getValue().isEmpty()) {
        Double.parseDouble(inputTo.getValue());
        valid = true;
      }
    } catch (Exception e) {
      valid = false;
    }

    return valid;
  }

  public abstract void setInputFromFilterParam(FilterParameter filterParam);

  public abstract void setup();

  public abstract FilterParameter getFilter();

  public abstract void clear();
}
