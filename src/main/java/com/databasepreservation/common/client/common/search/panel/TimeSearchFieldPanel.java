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
import com.tractionsoftware.gwt.user.client.ui.UTCTimeBox;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class TimeSearchFieldPanel extends SearchFieldPanel {
  private static final DateTimeFormat timeFormat = DateTimeFormat
    .getFormat(DateTimeFormat.PredefinedFormat.HOUR24_MINUTE_SECOND);

  private final UTCTimeBox inputTimeFromForTime;
  private final UTCTimeBox inputTimeToForTime;
  private final Label labelTo;

  public TimeSearchFieldPanel(SearchField searchField) {
    super(searchField);
    this.inputTimeFromForTime = new UTCTimeBox(timeFormat);
    this.inputTimeToForTime = new UTCTimeBox(timeFormat);
    this.labelTo = new Label(messages.searchingRange_to());
  }

  @Override
  public void setInputFromFilterParam(FilterParameter filterParam) {
    if (filterParam instanceof DateRangeFilterParameter) {
      DateRangeFilterParameter dateRangeFilterParameter = (DateRangeFilterParameter) filterParam;
      if (filterParam.getName().endsWith(ViewerConstants.SOLR_DYN_TDATETIME)) {
        if (dateRangeFilterParameter.getFromValue() != null) {
          inputTimeFromForTime.setValue(dateRangeFilterParameter.getFromValue().getTime());
        }

        if (dateRangeFilterParameter.getToValue() != null) {
          inputTimeToForTime.setValue(dateRangeFilterParameter.getToValue().getTime());
        }
      }
    }
  }

  @Override
  public void setup() {
    this.inputTimeFromForTime.addStyleName(FORM_TEXTBOX_FORM_TEXTBOX_SMALL);
    this.inputTimeToForTime.addStyleName(FORM_TEXTBOX_FORM_TEXTBOX_SMALL);

    labelTo.addStyleName(LABEL);

    inputPanel.add(inputTimeFromForTime);
    inputPanel.add(labelTo);
    inputPanel.add(inputTimeToForTime);
  }

  @Override
  public FilterParameter getFilter() {
    FilterParameter filterParameter = null;
    List<String> searchFields = getSearchField().getSearchFields();

    if (searchFields != null && searchFields.size() >= 1) {
      String field = searchFields.get(0);
      if (inputTimeFromForTime.getValue() != null || inputTimeToForTime != null) {
        if (inputTimeFromForTime.getValue() == null) {
          inputTimeFromForTime.setValue(inputTimeToForTime.getValue());
        } else if (inputTimeToForTime.getValue() == null) {
          inputTimeToForTime.setValue(inputTimeFromForTime.getValue());
        }

        if (dateIntervalValid(inputTimeFromForTime, inputTimeToForTime)) {
          Date begin = getDateFromInput(inputTimeFromForTime);
          Date end = getDateFromInput(inputTimeToForTime);
          filterParameter = new DateRangeFilterParameter(field, begin, end, RodaConstants.DateGranularity.MILLISECOND);
        } else {
          Dialogs.showErrors(messages.advancedSearchDialogTitle(), messages.advancedSearchErrorMessageForTimeInvalid(),
            messages.basicActionClose());
        }
      }
    }
    return filterParameter;
  }

  @Override
  public void clear() {
    inputTimeFromForTime.setText("");
    inputTimeToForTime.setText("");
  }
}
