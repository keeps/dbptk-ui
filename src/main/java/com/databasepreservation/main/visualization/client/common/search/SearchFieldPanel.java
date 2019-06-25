/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.main.visualization.client.common.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.DateRangeFilterParameter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.LongRangeFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;

import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.ClientLogger;
import com.databasepreservation.main.common.shared.client.common.search.SearchField;
import com.databasepreservation.main.common.shared.client.common.utils.ListboxUtils;
import com.databasepreservation.main.common.shared.client.tools.Humanize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;
import com.tractionsoftware.gwt.user.client.ui.UTCDateBox;
import com.tractionsoftware.gwt.user.client.ui.UTCTimeBox;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SearchFieldPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final DateTimeFormat timeFormat = DateTimeFormat
    .getFormat(DateTimeFormat.PredefinedFormat.HOUR24_MINUTE_SECOND);

  @SuppressWarnings("unused")
  private ClientLogger LOGGER = new ClientLogger(getClass().getName());

  private FlowPanel panel;
  private FlowPanel leftPanel;
  private FlowPanel inputPanel;
  private Button remove = new Button("<i class=\"fa fa-close\"></i>");

  private SearchField searchField;

  // Simple search field
  private Label fieldLabel;

  // Complex search field
  private ListBox searchAdvancedFields;
  private Map<String, SearchField> searchFields;

  // Column visibility in results
  private SimplePanel columnVisibilityPanel;
  private CheckBox columnVisibility;

  // any range
  private Label labelTo;

  // Text
  private TextBox inputText;
  // Date interval
  private UTCDateBox inputDateFromForDate;
  private UTCDateBox inputDateToForDate;
  // DateTime interval
  private UTCDateBox inputDateFromForDateTime;
  private UTCTimeBox inputTimeFromForDateTime;
  private UTCDateBox inputDateToForDateTime;
  private UTCTimeBox inputTimeToForDateTime;
  private Label labelAt1;
  private Label labelAt2;
  // Time interval
  private UTCTimeBox inputTimeFromForTime;
  private UTCTimeBox inputTimeToForTime;
  // Numeric
  private TextBox inputNumeric;
  // Numeric interval
  private TextBox inputNumericFrom;
  private TextBox inputNumericTo;
  // Storage
  private TextBox inputStorageSizeFrom;
  private TextBox inputStorageSizeTo;
  private ListBox inputStorageSizeList;
  // Boolean
  private CheckBox inputCheckBox;

  // Suggestion
  // private SearchSuggestBox<?> inputSearchSuggestBox = null;

  private Map<String, Boolean> columnDisplayNameToVisibleState = new HashMap<>();

  public SearchFieldPanel(Map<String, Boolean> columnDisplayNameToVisibleState) {
    this();
    this.columnDisplayNameToVisibleState = columnDisplayNameToVisibleState;
  }

  public SearchFieldPanel() {
    panel = new FlowPanel();
    leftPanel = new FlowPanel();
    inputPanel = new FlowPanel();
    fieldLabel = new Label();
    searchAdvancedFields = new ListBox();
    columnVisibility = new CheckBox();

    setVisibilityCheckboxValue(true, false);

    columnVisibilityPanel = new SimplePanel(columnVisibility);

    DateBox.DefaultFormat dateFormat = new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));

    inputText = new TextBox();

    inputDateFromForDate = new UTCDateBox();
    inputDateFromForDate.getDateBox().setFormat(dateFormat);
    inputDateFromForDate.getDateBox().getDatePicker().setYearArrowsVisible(true);
    inputDateFromForDate.getDateBox().setFireNullValues(true);
    inputDateFromForDate.getElement().setPropertyString("placeholder", messages.searchFieldDateFromPlaceHolder());

    inputDateToForDate = new UTCDateBox();
    inputDateToForDate.getDateBox().setFormat(dateFormat);
    inputDateToForDate.getDateBox().getDatePicker().setYearArrowsVisible(true);
    inputDateToForDate.getDateBox().setFireNullValues(true);
    inputDateToForDate.getElement().setPropertyString("placeholder", messages.searchFieldDateToPlaceHolder());

    inputDateFromForDateTime = new UTCDateBox();
    inputDateFromForDateTime.getDateBox().setFormat(dateFormat);
    inputDateFromForDateTime.getDateBox().getDatePicker().setYearArrowsVisible(true);
    inputDateFromForDateTime.getDateBox().setFireNullValues(true);
    inputDateFromForDateTime.getElement().setPropertyString("placeholder", messages.searchFieldDateFromPlaceHolder());

    inputTimeFromForDateTime = new UTCTimeBox(timeFormat);
    inputTimeFromForDateTime.getElement().setPropertyString("placeholder", messages.searchFieldTimeFromPlaceHolder());

    inputDateToForDateTime = new UTCDateBox();
    inputDateToForDateTime.getDateBox().setFormat(dateFormat);
    inputDateToForDateTime.getDateBox().getDatePicker().setYearArrowsVisible(true);
    inputDateToForDateTime.getDateBox().setFireNullValues(true);
    inputDateToForDateTime.getElement().setPropertyString("placeholder", messages.searchFieldDateToPlaceHolder());

    inputTimeToForDateTime = new UTCTimeBox(timeFormat);
    inputTimeToForDateTime.getElement().setPropertyString("placeholder", messages.searchFieldTimeToPlaceHolder());

    inputTimeFromForTime = new UTCTimeBox(timeFormat);
    inputTimeFromForTime.getElement().setPropertyString("placeholder", messages.searchFieldTimeFromPlaceHolder());

    inputTimeToForTime = new UTCTimeBox(timeFormat);
    inputTimeToForTime.getElement().setPropertyString("placeholder", messages.searchFieldTimeToPlaceHolder());

    inputNumeric = new TextBox();
    inputNumeric.getElement().setPropertyString("placeholder", messages.searchFieldNumericPlaceHolder());
    inputNumeric.getElement().setAttribute("type", "number");
    inputNumericFrom = new TextBox();
    inputNumericFrom.getElement().setPropertyString("placeholder", messages.searchFieldNumericFromPlaceHolder());
    inputNumericFrom.getElement().setAttribute("type", "number");
    inputNumericTo = new TextBox();
    inputNumericTo.getElement().setPropertyString("placeholder", messages.searchFieldNumericToPlaceHolder());
    inputNumericTo.getElement().setAttribute("type", "number");

    inputStorageSizeFrom = new TextBox();
    inputStorageSizeFrom.getElement().setPropertyString("placeholder", messages.searchFieldNumericFromPlaceHolder());
    inputStorageSizeFrom.getElement().setAttribute("type", "number");
    inputStorageSizeTo = new TextBox();
    inputStorageSizeTo.getElement().setPropertyString("placeholder", messages.searchFieldNumericToPlaceHolder());
    inputStorageSizeTo.getElement().setAttribute("type", "number");
    inputStorageSizeList = new ListBox();
    for (String unit : Humanize.UNITS) {
      inputStorageSizeList.addItem(unit, unit);
    }

    inputCheckBox = new CheckBox();

    labelTo = new Label(messages.searchingRange_to());
    labelAt1 = new Label(messages.searchingTime_at());
    labelAt2 = new Label(messages.searchingTime_at());

    panel.add(leftPanel);

    initWidget(panel);

    searchAdvancedFields.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        listBoxSearchField(searchAdvancedFields.getSelectedValue());

        // handle checkbox
        String columnDisplayName = getSearchField().getLabel();
        if (columnDisplayNameToVisibleState.containsKey(columnDisplayName)) {
          columnVisibility.setValue(columnDisplayNameToVisibleState.get(columnDisplayName), false);
        } else {
          columnVisibility.setValue(true, false);
        }
      }
    });

    panel.addStyleName("search-field");
    leftPanel.addStyleName("search-field-left-panel");
    inputPanel.addStyleName("search-field-input-panel");
    inputPanel.addStyleName("full_width");
    remove.addStyleName("search-field-remove");
    fieldLabel.addStyleName("search-field-label");
    searchAdvancedFields.addStyleName("form-listbox");
    // columnVisibilityPanel.setStyleName("form-listbox search-field-input-panel");
    columnVisibility.setStyleName("visibility-checkbox");

    labelTo.addStyleName("label");
    labelAt1.addStyleName("label");
    labelAt2.addStyleName("label");
    inputText.addStyleName("form-textbox");
    inputDateFromForDate.addStyleName("form-textbox form-textbox-small");
    inputDateToForDate.addStyleName("form-textbox form-textbox-small");
    inputDateFromForDateTime.addStyleName("form-textbox form-textbox-small");
    inputDateToForDateTime.addStyleName("form-textbox form-textbox-small");
    inputTimeFromForDateTime.addStyleName("form-textbox form-textbox-small");
    inputTimeToForDateTime.addStyleName("form-textbox form-textbox-small");
    inputTimeFromForTime.addStyleName("form-textbox form-textbox-small");
    inputTimeToForTime.addStyleName("form-textbox form-textbox-small");
    inputNumeric.addStyleName("form-textbox form-textbox-small");
    inputNumericFrom.addStyleName("form-textbox form-textbox-small");
    inputNumericTo.addStyleName("form-textbox form-textbox-small");
    inputStorageSizeFrom.addStyleName("form-textbox form-textbox-small");
    inputStorageSizeTo.addStyleName("form-textbox form-textbox-small");
    inputStorageSizeList.addStyleName("form-listbox");
    inputCheckBox.addStyleName("form-checkbox");
  }

  public SearchField getSearchField() {
    return searchField;
  }

  public void setSearchField(SearchField searchField) {
    this.searchField = searchField;

    Boolean visibleState = columnDisplayNameToVisibleState.get(searchField.getLabel());
    setVisibilityCheckboxValue(visibleState == null || visibleState, false);
  }

  public void setSearchAdvancedFields(ListBox searchAdvancedFieldOptions) {
    ListboxUtils.copyValues(searchAdvancedFieldOptions, searchAdvancedFields);
  }

  public void setSearchFields(Map<String, SearchField> searchFields) {
    this.searchFields = searchFields;
  }

  public void selectSearchField(String field) {
    ListboxUtils.select(searchAdvancedFields, field);
    listBoxSearchField(field);
  }

  public void selectFirstSearchField() {
    if (searchAdvancedFields.getItemCount() > 0) {
      listBoxSearchField(searchAdvancedFields.getValue(0));
    }
  }

  public void addRemoveClickHandler(ClickHandler clickHandler) {
    remove.addClickHandler(clickHandler);
  }

  public String getField() {
    return searchAdvancedFields.getSelectedValue();
  }

  public void setInputFromFilterParam(FilterParameter filterParam) {
    if (filterParam instanceof DateRangeFilterParameter) {
      DateRangeFilterParameter dateRangeFilterParameter = (DateRangeFilterParameter) filterParam;

      long begin = UTCDateBox.date2utc(dateRangeFilterParameter.getFromValue());
      long end = UTCDateBox.date2utc(dateRangeFilterParameter.getToValue());

      if (filterParam.getName().endsWith(ViewerConstants.SOLR_DYN_TDATE)) {
        inputDateFromForDate.setValue(begin);
        inputDateToForDate.setValue(end);
      } else if (filterParam.getName().endsWith(ViewerConstants.SOLR_DYN_TTIME)) {
        inputTimeFromForTime.setValue(begin);
        inputTimeToForTime.setValue(end);
      } else if (filterParam.getName().endsWith(ViewerConstants.SOLR_DYN_TDATETIME)) {
        inputDateFromForDateTime.setValue(begin);
        inputTimeFromForDateTime
          .setValue(extractTimePartWithTimeZone(dateRangeFilterParameter.getFromValue(), inputDateFromForDateTime));
        inputDateToForDateTime.setValue(end);
        inputTimeToForDateTime
          .setValue(extractTimePartWithTimeZone(dateRangeFilterParameter.getToValue(), inputDateToForDateTime));
      }
    } else if (filterParam instanceof LongRangeFilterParameter) {
      LongRangeFilterParameter longRangeFilterParameter = (LongRangeFilterParameter) filterParam;

      Long begin = longRangeFilterParameter.getFromValue();
      Long end = longRangeFilterParameter.getToValue();

      if (begin != null && end != null) {
        inputNumericFrom.setValue(String.valueOf(begin));
        inputNumericTo.setValue(String.valueOf(end));
      }
    } else if (filterParam instanceof BasicSearchFilterParameter) {
      BasicSearchFilterParameter basicSearchFilterParameter = (BasicSearchFilterParameter) filterParam;
      inputText.setValue(basicSearchFilterParameter.getValue());
      GWT.log("set " + getField() + " to " + basicSearchFilterParameter.getValue());
    }
  }

  /**
   * Removes the date part (year, month, day) from a Date that contains date and
   * time (keeping whichever timezone is on the passed Date)
   */
  private long extractTimePartWithTimeZone(Date dateAndTime, UTCDateBox sameDateInDateBox) {
    // sameDateInDateBox.getValue returns UTC time, subtracting them obtains a
    // time with timezone.
    return dateAndTime.getTime() - sameDateInDateBox.getValue();
  }

  public FilterParameter getFilter() {
    FilterParameter filterParameter = null;
    String type = searchField.getType();
    List<String> searchFields = searchField.getSearchFields();

    if (searchFields != null && searchFields.size() >= 1) {
      String field = searchFields.get(0);

      if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_DATE)
        && (inputDateFromForDate.getValue() != null || inputDateToForDate.getValue() != null)) {

        if (inputDateFromForDate.getValue() == null) {
          inputDateFromForDate.setValue(inputDateToForDate.getValue());
        } else if (inputDateToForDate.getValue() == null) {
          inputDateToForDate.setValue(inputDateFromForDate.getValue());
        }

        if (dateIntervalValid(inputDateFromForDate, inputDateToForDate)) {
          Date begin = getDateFromInput(inputDateFromForDate);
          Date end = getDateFromInput(inputDateToForDate);
          filterParameter = new DateRangeFilterParameter(field, begin, end, RodaConstants.DateGranularity.DAY);
          // } else if
          // (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL)
          // && dateIntervalValid(inputDateFromForDate, inputDateToForDate) &&
          // searchFields.size() >= 2) {
          // String fieldTo = searchField.getSearchFields().get(1);
          // filterParameter = new DateIntervalFilterParameter(field, fieldTo,
          // inputDateBoxFrom.getValue(),
          // inputDateBoxTo.getValue());
          // } else if
          // (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL)
          // && dateIntervalValid(inputDateBoxFrom, inputDateBoxTo)) {
          // filterParameter = new DateIntervalFilterParameter(field, field,
          // inputDateBoxFrom.getValue(),
          // inputDateBoxTo.getValue());
        } else {
          GWT.log("date interval was invalid");
        }
      } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_TIME)
        && (inputTimeFromForTime.getValue() != null || inputTimeToForTime != null)) {

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
          GWT.log("time interval was invalid");
        }
      } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_DATETIME)
        && (inputDateFromForDateTime.getValue() != null || inputDateToForDateTime.getValue() != null)) {

        if (inputDateFromForDateTime.getValue() == null) {
          inputDateFromForDateTime.setValue(inputDateToForDateTime.getValue());
        } else if (inputDateToForDateTime.getValue() == null) {
          inputDateToForDateTime.setValue(inputDateFromForDateTime.getValue());
        }

        if (inputDateFromForDateTime.getValue().equals(inputDateToForDateTime.getValue())) {
          // dates are equal
          // if we have one of the times, use it for both times
          // if we have no times, use 00:00:00.000 to 23:59:59.999
          // if the times are both defined, let them be
          if (inputTimeFromForDateTime.getValue() == null && inputTimeToForDateTime.getValue() != null) {
            inputTimeFromForDateTime.setValue(inputTimeToForDateTime.getValue());
          } else if (inputTimeFromForDateTime.getValue() != null && inputTimeToForDateTime.getValue() == null) {
            inputTimeToForDateTime.setValue(inputTimeFromForDateTime.getValue());
          } else if (inputTimeFromForDateTime.getValue() == null && inputTimeToForDateTime.getValue() == null) {
            inputTimeFromForDateTime.setValue(0L);
            inputTimeToForDateTime.setValue(ViewerConstants.MILLISECONDS_IN_A_DAY - 1);
          }
        } else {
          // dates are different
          // use 00:00:00.000 for the "from" if it is missing
          // use 23:59:59.999 for the "to" if it is missing
          if (inputTimeFromForDateTime.getValue() == null) {
            inputTimeFromForDateTime.setValue(0L);
          }

          if (inputTimeToForDateTime.getValue() == null) {
            inputTimeToForDateTime.setValue(ViewerConstants.MILLISECONDS_IN_A_DAY - 1);
          }
        }

        if (dateIntervalValid(inputDateFromForDateTime, inputTimeFromForDateTime, inputDateToForDateTime,
          inputTimeToForDateTime)) {
          Date begin = getDateFromInput(inputDateFromForDateTime, inputTimeFromForDateTime);
          Date end = getDateFromInput(inputDateToForDateTime, inputTimeToForDateTime);
          filterParameter = new DateRangeFilterParameter(field, begin, end, RodaConstants.DateGranularity.MILLISECOND);
        } else {
          GWT.log("datetime interval was invalid");
        }
      } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_NUMERIC) && valid(inputNumeric)) {
        filterParameter = new BasicSearchFilterParameter(field, inputNumeric.getValue());
      } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_NUMERIC_INTERVAL)
        && (!inputNumericFrom.getValue().isEmpty() || !inputNumericTo.getValue().isEmpty())) {

        if (inputNumericFrom.getValue().isEmpty()) {
          inputNumericFrom.setValue(inputNumericTo.getValue());
        } else if (inputNumericTo.getValue().isEmpty()) {
          inputNumericTo.setValue(inputNumericFrom.getValue());
        }

        if (intervalValid(inputNumericFrom, inputNumericTo)) {
          filterParameter = new LongRangeFilterParameter(field, Long.valueOf(inputNumericFrom.getValue()),
            Long.valueOf(inputNumericTo.getValue()));
        } else {
          GWT.log("numeric interval was invalid");
        }
      } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_STORAGE)
        && intervalValid(inputStorageSizeFrom, inputStorageSizeTo)) {
        filterParameter = new LongRangeFilterParameter(field,
          Humanize.parseFileSize(inputStorageSizeFrom.getValue(), inputStorageSizeList.getSelectedValue()),
          Humanize.parseFileSize(inputStorageSizeTo.getValue(), inputStorageSizeList.getSelectedValue()));
      } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_BOOLEAN) && valid(inputCheckBox)) {
        filterParameter = new SimpleFilterParameter(field, Boolean.toString(inputCheckBox.getValue()));
        // } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_SUGGEST)
        // &&
        // valid(inputSearchSuggestBox)) {
        // filterParameter = new SimpleFilterParameter(field,
        // inputSearchSuggestBox.getValue());
      } else if (valid(inputText)) {
        filterParameter = new BasicSearchFilterParameter(field, inputText.getValue());
      }
    }

    return filterParameter;
  }

  private Date getDateFromInput(UTCDateBox dateBox) {
    return new Date(dateBox.getValue());
  }

  private Date getDateFromInput(UTCTimeBox timeBox) {
    return new Date(timeBox.getValue());
  }

  private Date getDateFromInput(UTCDateBox dateBox, UTCTimeBox timeBox) {
    return new Date(dateBox.getValue() + timeBox.getValue());
  }

  public void listBoxSearchField(String field) {
    SearchField searchField = searchFields.get(field);
    setSearchField(searchField);

    leftPanel.clear();
    leftPanel.add(columnVisibility);
    leftPanel.add(searchAdvancedFields);
    leftPanel.add(inputPanel);
    setInputPanel(searchField.getType());
    panel.add(remove);
    panel.removeStyleName("full_width");
  }

  public void simpleSearchField(String field, String label, String type) {
    List<String> searchFields = new ArrayList<String>();
    searchFields.add(field);
    setSearchField(new SearchField(field, searchFields, label, type));

    fieldLabel.setText(label);
    leftPanel.clear();
    leftPanel.add(fieldLabel);
    leftPanel.add(columnVisibilityPanel);
    leftPanel.add(inputPanel);
    setInputPanel(type);
    panel.addStyleName("full_width");
  }

  private void setInputPanel(String type) {
    inputPanel.clear();
    inputPanel.removeStyleName("full_width");

    if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_DATE)) {
      inputPanel.add(inputDateFromForDate);
      inputPanel.add(labelTo);
      inputPanel.add(inputDateToForDate);
    } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_TIME)) {
      inputPanel.add(inputTimeFromForTime);
      inputPanel.add(labelTo);
      inputPanel.add(inputTimeToForTime);
    } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_DATETIME)) {
      inputPanel.add(inputDateFromForDateTime);
      inputPanel.add(labelAt1);
      inputPanel.add(inputTimeFromForDateTime);
      inputPanel.add(labelTo);
      inputPanel.add(inputDateToForDateTime);
      inputPanel.add(labelAt2);
      inputPanel.add(inputTimeToForDateTime);
    } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_DATE_INTERVAL)) {
      // TODO: support date interval
    } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_NUMERIC)) {
      inputPanel.add(inputNumeric);
    } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_NUMERIC_INTERVAL)) {
      inputPanel.add(inputNumericFrom);
      inputPanel.add(labelTo);
      inputPanel.add(inputNumericTo);
    } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_STORAGE)) {
      inputPanel.add(inputStorageSizeFrom);
      inputPanel.add(inputStorageSizeTo);
      inputPanel.add(inputStorageSizeList);
    } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_BOOLEAN)) {
      inputPanel.add(inputCheckBox);
    } else if (type.equals(ViewerConstants.SEARCH_FIELD_TYPE_SUGGEST)) {
    } else {
      inputPanel.add(inputText);
      inputPanel.addStyleName("full_width");
    }
  }

  // public void addInputSearchSuggestBox(SearchSuggestBox<?> searchSuggestBox)
  // {
  // this.inputSearchSuggestBox = searchSuggestBox;
  // inputPanel.add(searchSuggestBox);
  // inputSearchSuggestBox.addStyleName("form-textbox");
  // inputPanel.addStyleName("full_width");
  // }

  private boolean dateIntervalValid(UTCDateBox inputFrom, UTCDateBox inputTo) {
    return inputFrom.getValue() != null && inputTo.getValue() != null && inputFrom.getValue() <= inputTo.getValue();
  }

  private boolean dateIntervalValid(UTCDateBox dateFrom, UTCTimeBox timeFrom, UTCDateBox dateTo, UTCTimeBox timeTo) {
    if (dateFrom.getValue() != null && timeFrom.getValue() != null && dateTo.getValue() != null
      && timeTo.getValue() != null) {
      return dateFrom.getValue() + timeFrom.getValue() <= dateTo.getValue() + timeTo.getValue();
    }
    return false;
  }

  private boolean dateIntervalValid(UTCTimeBox inputFrom, UTCTimeBox inputTo) {
    return inputFrom.getValue() != null && inputTo.getValue() != null && inputFrom.getValue() <= inputTo.getValue();
  }

  private boolean dateValid(UTCDateBox input) {
    return (input.getValue() != null);
  }

  private boolean valid(TextBox input) {
    return (!input.getValue().isEmpty());
  }

  // private boolean valid(SearchSuggestBox<?> input) {
  // return (!input.getValue().isEmpty());
  // }

  private boolean valid(CheckBox input) {
    return input.getValue();
  }

  private boolean intervalValid(TextBox inputFrom, TextBox inputTo) {
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

  public void setVisibilityChangedHandler(ValueChangeHandler<Boolean> handler) {
    columnVisibility.addValueChangeHandler(handler);
  }

  public void setVisibilityCheckboxValue(boolean value, boolean triggerEvents) {
    columnVisibility.setValue(value, triggerEvents);
  }

  public Boolean getVisibilityCheckboxValue() {
    return columnVisibility.getValue();
  }
}
