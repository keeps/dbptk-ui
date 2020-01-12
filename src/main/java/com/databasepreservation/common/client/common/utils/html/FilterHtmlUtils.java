package com.databasepreservation.common.client.common.utils.html;

import java.util.List;

import com.databasepreservation.common.client.index.filter.BasicSearchFilterParameter;
import com.databasepreservation.common.client.index.filter.DateRangeFilterParameter;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.FilterParameter;
import com.databasepreservation.common.client.index.filter.LongRangeFilterParameter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;

import com.databasepreservation.common.client.tools.Humanize;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class FilterHtmlUtils {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static SafeHtml getFilterHTML(Filter filter) {
    List<FilterParameter> parameterValues = filter.getParameters();

    if (parameterValues.isEmpty()) {
      return SafeHtmlUtils.fromTrustedString(messages.filterParameterEmpty());
    }

    SafeHtmlBuilder preFilterTranslations = new SafeHtmlBuilder();
    preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant("<ul><li>"));

    for (int i = 0; i < parameterValues.size(); i++) {
      final SafeHtml filterParameterHTML = getFilterParameterHTML(parameterValues.get(i));
      if (filterParameterHTML != null) {
        preFilterTranslations.append(filterParameterHTML);
      }

      preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant("</li>"));
      if (i != parameterValues.size() - 1) {
        preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant("<li>"));
      }
    }

    preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant("</ul>"));
    return preFilterTranslations.toSafeHtml();
  }

  public static SafeHtml getFilterParameterHTML(FilterParameter parameter) {
    if (parameter instanceof SimpleFilterParameter) {
      SimpleFilterParameter p = (SimpleFilterParameter) parameter;
      return messages.simpleFilterParameter(messages.activityLogFilterName(p.getName()), p.getValue());
    } else if (parameter instanceof BasicSearchFilterParameter) {
      BasicSearchFilterParameter p = (BasicSearchFilterParameter) parameter;
      // TODO put '*' in some constant, see Search
      if (!"*".equals(p.getValue())) {
        return messages.basicFilterParameter(messages.activityLogFilterName(p.getName()), p.getValue());
      }
    } else if (parameter instanceof LongRangeFilterParameter) {
      LongRangeFilterParameter p = (LongRangeFilterParameter) parameter;
      if (p.getFromValue() == null) {
        return messages.longRangeFilterParameterOnlyTo(messages.activityLogFilterName(p.getName()), p.getToValue());
      } else if (p.getToValue() == null) {
        return messages.longRangeFilterParameterOnlyFrom(messages.activityLogFilterName(p.getName()), p.getFromValue());
      } else {
        if (p.getToValue().equals(p.getFromValue())) {
          return messages.longRangeFilterParameterEquals(messages.activityLogFilterName(p.getName()), p.getFromValue());
        }
        return messages.longRangeFilterParameter(messages.activityLogFilterName(p.getName()), p.getFromValue(),
          p.getToValue());
      }
    } else if (parameter instanceof DateRangeFilterParameter) {
      DateRangeFilterParameter p = (DateRangeFilterParameter) parameter;
      SafeHtmlBuilder builder = new SafeHtmlBuilder();
      if (p.getFromValue() != null && p.getToValue() != null) {
        return messages.dateRangeFilterParameter(Humanize.formatDateTime(p.getFromValue(), true),
          Humanize.formatDateTime(p.getToValue(), true));
      } else if (p.getToValue() != null) {
        return messages.dateRangeFilterParameterOnlyTo(Humanize.formatDateTime(p.getToValue(), true));
      } else {
        return messages.dateRangeFilterParameterOnlyFrom(Humanize.formatDateTime(p.getFromValue(), true));
      }
    } else {
      return SafeHtmlUtils.fromString(parameter.getClass().getSimpleName());
    }

    return null;
  }
}
