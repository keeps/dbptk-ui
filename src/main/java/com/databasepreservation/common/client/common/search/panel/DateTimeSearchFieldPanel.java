/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.common.search.panel;

import java.util.Date;
import java.util.List;

import org.roda.core.data.common.RodaConstants;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.search.SearchField;
import com.databasepreservation.common.client.index.filter.DateRangeFilterParameter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.datepicker.client.DateBox;
import com.tractionsoftware.gwt.user.client.ui.UTCDateBox;
import com.tractionsoftware.gwt.user.client.ui.UTCTimeBox;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DateTimeSearchFieldPanel extends SearchFieldPanel {
  private static final DateBox.DefaultFormat dateFormat = new DateBox.DefaultFormat(
    DateTimeFormat.getFormat("yyyy-MM-dd"));
  private static final DateTimeFormat timeFormat = DateTimeFormat
    .getFormat(DateTimeFormat.PredefinedFormat.HOUR24_MINUTE_SECOND);

  // DateTime interval
  private final UTCDateBox inputDateFromForDateTime;
  private final UTCTimeBox inputTimeFromForDateTime;
  private final UTCDateBox inputDateToForDateTime;
  private final UTCTimeBox inputTimeToForDateTime;
  private final Label labelTo;
  private final Label labelAt1;
  private final Label labelAt2;

  public DateTimeSearchFieldPanel(SearchField searchField) {
    super(searchField);
    inputDateFromForDateTime = new UTCDateBox();
    inputDateFromForDateTime.getDateBox().setFormat(dateFormat);
    inputDateFromForDateTime.getDateBox().getDatePicker().setYearArrowsVisible(true);

    inputTimeFromForDateTime = new UTCTimeBox(timeFormat);

    inputDateToForDateTime = new UTCDateBox();
    inputDateToForDateTime.getDateBox().setFormat(dateFormat);
    inputDateToForDateTime.getDateBox().getDatePicker().setYearArrowsVisible(true);

    inputTimeToForDateTime = new UTCTimeBox(timeFormat);

    inputDateFromForDateTime.addStyleName(FORM_TEXTBOX_FORM_TEXTBOX_SMALL);
    inputDateToForDateTime.addStyleName(FORM_TEXTBOX_FORM_TEXTBOX_SMALL);
    inputTimeFromForDateTime.addStyleName(FORM_TEXTBOX_FORM_TEXTBOX_SMALL);
    inputTimeToForDateTime.addStyleName(FORM_TEXTBOX_FORM_TEXTBOX_SMALL);

    labelTo = new Label(messages.searchingRange_to());
    labelAt1 = new Label(messages.searchingTime_at());
    labelAt2 = new Label(messages.searchingTime_at());

    labelTo.addStyleName(LABEL);
    labelAt1.addStyleName(LABEL);
    labelAt2.addStyleName(LABEL);
  }

  @Override
  public void setInputFromFilterParam(FilterParameter filterParam) {
    if (filterParam instanceof DateRangeFilterParameter) {
      DateRangeFilterParameter dateRangeFilterParameter = (DateRangeFilterParameter) filterParam;
      if (filterParam.getName().endsWith(ViewerConstants.SOLR_DYN_TDATETIME)) {
        if (dateRangeFilterParameter.getFromValue() != null) {
          inputDateFromForDateTime.setValue(UTCDateBox.date2utc(dateRangeFilterParameter.getFromValue()));
          inputTimeFromForDateTime
            .setValue(extractTimePartWithTimeZone(dateRangeFilterParameter.getFromValue(), inputDateFromForDateTime));
        }

        if (dateRangeFilterParameter.getToValue() != null) {
          inputDateToForDateTime.setValue(UTCDateBox.date2utc(dateRangeFilterParameter.getToValue()));
          inputTimeToForDateTime
            .setValue(extractTimePartWithTimeZone(dateRangeFilterParameter.getToValue(), inputDateToForDateTime));
        }
      }
    }
  }

  @Override
  public void setup() {
    inputPanel.add(inputDateFromForDateTime);
    inputPanel.add(labelAt1);
    inputPanel.add(inputTimeFromForDateTime);
    inputPanel.add(labelTo);
    inputPanel.add(inputDateToForDateTime);
    inputPanel.add(labelAt2);
    inputPanel.add(inputTimeToForDateTime);
  }

  @Override
  public FilterParameter getFilter() {
    FilterParameter filterParameter = null;
    List<String> searchFields = getSearchField().getSearchFields();

    if (searchFields != null && searchFields.size() >= 1) {
      String field = searchFields.get(0);

      if (inputDateFromForDateTime.getValue() != null || inputDateToForDateTime.getValue() != null) {
        if (inputDateFromForDateTime.getValue() != null
          && inputDateFromForDateTime.getValue().equals(inputDateToForDateTime.getValue())) {
          if (inputTimeFromForDateTime.getValue() == null && inputTimeToForDateTime.getValue() != null) {
            inputTimeFromForDateTime.setValue(0L);
          } else if (inputTimeFromForDateTime.getValue() != null && inputTimeToForDateTime.getValue() == null) {
            inputTimeToForDateTime.setValue(ViewerConstants.MILLISECONDS_IN_A_DAY - 1);
          } else if (inputTimeFromForDateTime.getValue() == null && inputTimeToForDateTime.getValue() == null) {
            inputTimeFromForDateTime.setValue(0L);
            inputTimeToForDateTime.setValue(ViewerConstants.MILLISECONDS_IN_A_DAY - 1);
          }
        } else {
          // dates are different
          // use 00:00:00.000 for the "from" if it is missing
          // use 23:59:59.999 for the "to" if it is missing
          if (inputDateFromForDateTime.getValue() != null && inputTimeFromForDateTime.getValue() == null) {
            inputTimeFromForDateTime.setValue(0L);
          }

          if (inputDateToForDateTime.getValue() != null && inputTimeToForDateTime.getValue() == null) {
            inputTimeToForDateTime.setValue(ViewerConstants.MILLISECONDS_IN_A_DAY - 1);
          }
        }

        if (dateIntervalValid(inputDateFromForDateTime, inputTimeFromForDateTime, inputDateToForDateTime,
          inputTimeToForDateTime)) {
          if (inputDateToForDateTime.getValue() == null) {
            Date begin = getDateFromInput(inputDateFromForDateTime, inputTimeFromForDateTime);
            filterParameter = new DateRangeFilterParameter(field, begin, inputDateToForDateTime.getDateBox().getValue(),
              RodaConstants.DateGranularity.MILLISECOND);
          } else if (inputDateFromForDateTime.getValue() == null) {
            Date end = getDateFromInput(inputDateToForDateTime, inputTimeToForDateTime);
            filterParameter = new DateRangeFilterParameter(field, inputDateFromForDateTime.getDateBox().getValue(), end,
              RodaConstants.DateGranularity.MILLISECOND);
          } else {
            Date begin = getDateFromInput(inputDateFromForDateTime, inputTimeFromForDateTime);
            Date end = getDateFromInput(inputDateToForDateTime, inputTimeToForDateTime);
            filterParameter = new DateRangeFilterParameter(field, begin, end,
              RodaConstants.DateGranularity.MILLISECOND);
          }
        } else {
          Dialogs.showErrors(messages.advancedSearchDialogTitle(), messages.advancedSearchErrorMessageForDateInvalid(),
            messages.basicActionClose());
        }
      }
    }
    return filterParameter;
  }

  @Override
  public void clear() {
    inputDateFromForDateTime.setText("");
    inputTimeFromForDateTime.setText("");
    inputDateToForDateTime.setText("");
    inputTimeToForDateTime.setText("");
    inputDateFromForDateTime.getDateBox().setValue(null);
    inputDateToForDateTime.getDateBox().setValue(null);
  }
}
