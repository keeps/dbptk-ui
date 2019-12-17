package com.databasepreservation.common.utils;

import java.io.Serializable;
import java.util.Locale;
import java.util.MissingResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.FacetFieldResult;
import com.databasepreservation.common.client.index.facets.FacetValue;
import com.databasepreservation.common.client.models.activity.logs.ActivityLogEntry;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.server.Messages;
import com.databasepreservation.common.server.ServerTools;
import com.databasepreservation.common.server.ViewerConfiguration;

public class I18nUtility {
  private static final Logger LOGGER = LoggerFactory.getLogger(I18nUtility.class);

  /** Private empty constructor */
  private I18nUtility() {

  }

  public static <T extends Serializable> IndexResult<T> translate(IndexResult<T> input, Class<T> resultClass,
    String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    return translate(input, resultClass, locale);
  }

  public static <T extends Serializable> IndexResult<T> translate(IndexResult<T> input, Class<T> resultClass,
    Locale locale) {
    if (input != null && input.getFacetResults() != null && !input.getFacetResults().isEmpty()) {
      for (FacetFieldResult ffr : input.getFacetResults()) {
        if (ffr != null && ffr.getValues() != null && !ffr.getValues().isEmpty()) {
          String field = ffr.getField();
          for (FacetValue fv : ffr.getValues()) {
            fv.setLabel(getFacetTranslation(field, fv.getValue(), locale, resultClass));
          }
        }
      }
    }

    return input;
  }

  private static <T extends Serializable> String getFacetTranslation(String facetField, String facetValue,
    Locale locale, Class<T> resultClass) {
    String ret;
    String bundleKey = ViewerConstants.I18N_UI_FACETS_PREFIX + "." + resultClass.getSimpleName() + "." + facetField
      + (facetValue == null || facetValue.trim().length() == 0 ? "other" : "." + facetValue.trim());

    try {
      if (resultClass.equals(ActivityLogEntry.class)
        && facetField.equals(ViewerConstants.SOLR_ACTIVITY_LOG_ACTION_METHOD) && facetValue != null) {
        ret = ViewerStringUtils.getPrettifiedActionMethod(facetValue);
      } else {
        ret = ViewerConfiguration.getI18NMessages(locale).getTranslation(bundleKey);
      }
    } catch (MissingResourceException e) {
      ret = facetValue;
      LOGGER.trace("Translation not found: " + bundleKey + " locale: " + locale, e);
    }

    return ret;
  }

  public static String getMessage(String key, String defaultMessage, String localeString) {
    Locale locale = ServerTools.parseLocale(localeString);
    Messages messages = ViewerConfiguration.getI18NMessages(locale);
    return messages.getTranslation(key, defaultMessage);
  }
}
