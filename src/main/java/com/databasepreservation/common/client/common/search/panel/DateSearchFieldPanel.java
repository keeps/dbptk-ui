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

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DateSearchFieldPanel extends SearchFieldPanel {
  DateBox.DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));

  private final UTCDateBox inputDateFromForDate;
  private final UTCDateBox inputDateToForDate;
  private final Label labelTo;

  public DateSearchFieldPanel(SearchField searchField) {
    super(searchField);
    inputDateFromForDate = new UTCDateBox();
    inputDateToForDate = new UTCDateBox();
    labelTo = new Label(messages.searchingRange_to());
  }

  @Override
  public void setInputFromFilterParam(FilterParameter filterParam) {
    if (filterParam instanceof DateRangeFilterParameter) {
      DateRangeFilterParameter dateRangeFilterParameter = (DateRangeFilterParameter) filterParam;
      if (filterParam.getName().endsWith(ViewerConstants.SOLR_DYN_TDATETIME)) {
        if (dateRangeFilterParameter.getFromValue() != null) {
          inputDateFromForDate.setValue(UTCDateBox.date2utc(dateRangeFilterParameter.getFromValue()));
        }

        if (dateRangeFilterParameter.getToValue() != null) {
          inputDateToForDate.setValue(UTCDateBox.date2utc(dateRangeFilterParameter.getToValue()));
        }
      }
    }
  }

  @Override
  public void setup() {
    this.inputDateFromForDate.getDateBox().setFormat(dateFormat);
    this.inputDateFromForDate.getDateBox().getDatePicker().setYearArrowsVisible(true);

    this.inputDateToForDate.getDateBox().setFormat(dateFormat);
    this.inputDateToForDate.getDateBox().getDatePicker().setYearArrowsVisible(true);

    this.inputDateFromForDate.addStyleName(FORM_TEXTBOX_FORM_TEXTBOX_SMALL);
    this.inputDateToForDate.addStyleName(FORM_TEXTBOX_FORM_TEXTBOX_SMALL);
    this.labelTo.addStyleName(LABEL);

    this.inputPanel.add(inputDateFromForDate);
    this.inputPanel.add(labelTo);
    this.inputPanel.add(inputDateToForDate);
  }

  @Override
  public FilterParameter getFilter() {
    FilterParameter filterParameter = null;
    List<String> searchFields = getSearchField().getSearchFields();

    if (searchFields != null && searchFields.size() >= 1) {
      String field = searchFields.get(0);

      if (inputDateFromForDate.getValue() != null || inputDateToForDate.getValue() != null) {
        if (inputDateToForDate.getValue() == null) {
          Date begin = getDateFromInput(inputDateFromForDate);
          filterParameter = new DateRangeFilterParameter(field, begin, inputDateToForDate.getDateBox().getValue(),
            RodaConstants.DateGranularity.DAY);
        } else if (inputDateFromForDate.getValue() == null) {
          Date end = getDateFromInput(inputDateToForDate);
          filterParameter = new DateRangeFilterParameter(field, inputDateFromForDate.getDateBox().getValue(), end,
            RodaConstants.DateGranularity.DAY);
        } else {
          Date begin = getDateFromInput(inputDateFromForDate);
          Date end = getDateFromInput(inputDateToForDate);
          filterParameter = new DateRangeFilterParameter(field, begin, end, RodaConstants.DateGranularity.DAY);
        }
      } else {
        Dialogs.showErrors(messages.advancedSearchDialogTitle(), messages.advancedSearchErrorMessageForDateInvalid(),
          messages.basicActionClose());
      }
    }

    return filterParameter;
  }

  @Override
  public void clear() {
    this.inputDateFromForDate.setText("");
    this.inputDateToForDate.setText("");
    this.inputDateFromForDate.getDateBox().setValue(null);
    this.inputDateToForDate.getDateBox().setValue(null);
  }
}
