package com.databasepreservation.common.client.common.utils;

import java.util.List;

import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.LongRangeFilterParameter;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;

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

  public static SafeHtml getFilterHTML(Filter filter, String classToFilter) {
    List<FilterParameter> parameterValues = filter.getParameters();
    SafeHtmlBuilder preFilterTranslations = new SafeHtmlBuilder();
    preFilterTranslations.append(SafeHtmlUtils.fromSafeConstant(classToFilter))
      .append(SafeHtmlUtils.fromSafeConstant("&nbsp;where")).append(SafeHtmlUtils.fromSafeConstant(":<ul><li>"));

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
    } else {
      return SafeHtmlUtils.fromString(parameter.getClass().getSimpleName());
    }

    return null;
  }
}
