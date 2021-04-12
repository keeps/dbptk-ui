/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.configuration.observer.CollectionObserver;
import com.databasepreservation.common.client.index.facets.FacetParameter;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.facets.SimpleFacetParameter;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ClientConfigurationManager {
  private static ClientLogger logger = new ClientLogger(ClientConfigurationManager.class.getName());
  // DO NOT ACCESS DIRECTLY, use getConfigurationProperties()
  private static ClientConfigurationManager instance = null;

  // DO NOT ACCESS DIRECTLY, use getConfigurationProperties()
  private Map<String, List<String>> configurationProperties = null;

  public static void initialize(Map<String, List<String>> properties) {
    instance = new ClientConfigurationManager(properties);
  }

  private ClientConfigurationManager(Map<String, List<String>> properties) {
    configurationProperties = properties;
  }

  private static Map<String, List<String>> getConfigurationProperties() {
    if (instance == null || instance.configurationProperties == null) {
      logger.error("Requiring a shared property while they are not yet loaded");
      return Collections.emptyMap();
    } else {
      return instance.configurationProperties;
    }
  }

  /**
   * @return The property value for the provided keyParts. Or {@code null} if the
   *         property value was null or the key is not present.
   */
  public static String getString(String... keyParts) {
    return getStringWithDefault(null, keyParts);
  }

  /**
   * @return The property value for the provided keyParts. Or {@code null} if the
   *         property value was null or the key is not present.
   */
  public static String getStringWithDefault(String defaultValue, String... keyParts) {
    List<String> values = getStringList(keyParts);
    return values.isEmpty() ? defaultValue : values.get(0);
  }

  public static <E extends Enum<E>> E getEnum(Class<E> enumType, E defaultValue, String... keyParts) {
    E ret = defaultValue;
    List<String> values = getStringList(keyParts);
    if (!values.isEmpty()) {
      try {
        ret = Enum.valueOf(enumType, values.get(0));
      } catch (IllegalArgumentException | NullPointerException e) {
        logger.error("Error parsing enum " + values.get(0), e);
        // proceed with returning the default
      }
    }
    return ret;
  }

  /**
   * @return The translation for the provided keyParts. Or {@code null} if the
   *         translation was null or the key is not present.
   */
  public static String getTranslation(String... keyParts) {
    return getTranslationWithDefault(null, keyParts);
  }

  public static String getTranslationWithDefault(String defaultValue, String... keyParts) {
    String translationKey = "i18n." + getConfigurationKey(keyParts);
    List<String> values = getStringList(translationKey);
    return values.isEmpty() ? defaultValue : values.get(0);
  }

  /**
   * Used when the configuration is setup as following:
   *
   * keyParts: i18nKey
   *
   * i18nKey: returnValue
   *
   * This method resolves the keyParts to an 18n key and retrieves its associated
   * translation.
   *
   * @return the translation for an i18n key that is the property value for the
   *         provided keyParts
   */
  public static String resolveTranslation(String... keyParts) {
    String i18nKey = getString(keyParts);
    if (i18nKey != null) {
      return getTranslation(i18nKey);
    }
    return null;
  }

  /**
   * @return The integer property value for the provided keyParts. Or
   *         {@code defaultValue} if the property value was null, not an integer
   *         or the key is not present.
   */
  public static Integer getInt(Integer defaultValue, String... keyParts) {
    String value = getString(keyParts);
    if (value != null) {
      try {
        return Integer.valueOf(value);
      } catch (NumberFormatException e) {
        // proceed with returning the default
      }
    }
    return defaultValue;
  }

  /**
   * @return The integer property value for the provided keyParts. Or {@code null}
   *         if the property value was null, not an integer or the key is not
   *         present.
   */
  public static Integer getInt(String... keyParts) {
    return getInt(null, keyParts);
  }

  /**
   * @return The double property value for the provided keyParts. Or
   *         {@code defaultValue} if the property value was null, not an double or
   *         the key is not present.
   */
  public static Double getDouble(Double defaultValue, String... keyParts) {
    String value = getString(keyParts);
    if (value != null) {
      try {
        return Double.valueOf(value);
      } catch (NumberFormatException e) {
        // proceed with returning the default
      }
    }
    return defaultValue;
  }

  /**
   * @return The property value for the provided keyParts. Or {@code null} if the
   *         property value was null or the key is not present.
   */
  public static boolean getBoolean(boolean defaultValue, String... keyParts) {
    String value = getString(keyParts);
    if ("true".equalsIgnoreCase(value)) {
      return true;
    } else if ("false".equalsIgnoreCase(value)) {
      return false;
    } else {
      return defaultValue;
    }
  }

  /**
   * @return A list with the property values for the provided keyParts. Or an
   *         empty list if the key is not present).
   *
   */
  public static List<String> getStringList(String... keyParts) {
    return getStringListWithDefault(new ArrayList<>(), keyParts);
  }

  public static List<String> getStringListWithDefault(List<String> defaultValue, String... keyParts) {
    String key = getConfigurationKey(keyParts);
    List<String> values = getConfigurationProperties().get(key);
    return values != null ? values : defaultValue;
  }

  private static String getConfigurationKey(String... keyParts) {
    StringBuilder sb = new StringBuilder();
    for (String part : keyParts) {
      if (sb.length() != 0) {
        sb.append('.');
      }
      sb.append(part);
    }
    return sb.toString();
  }

  public static class FacetFactory {
    private FacetFactory() {
      // do nothing
    }

    public static Facets getFacets(String listId) {
      String query = getString(ViewerConstants.LISTS_PROPERTY, listId, ViewerConstants.LISTS_FACETS_QUERY_PROPERTY);

      List<String> parameterNames = getStringList(ViewerConstants.LISTS_PROPERTY, listId,
        ViewerConstants.LISTS_FACETS_PARAMETERS_PROPERTY);

      if (!parameterNames.isEmpty()) {
        if (query != null) {
          return new Facets(buildParameters(listId, parameterNames), query);
        } else {
          return new Facets(buildParameters(listId, parameterNames));
        }
      } else {
        return Facets.NONE;
      }
    }

    private static Map<String, FacetParameter> buildParameters(String listId, List<String> parameterNames) {
      Map<String, FacetParameter> parameters = new HashMap<>();

      for (String parameterName : parameterNames) {
        String type = getString(ViewerConstants.LISTS_PROPERTY, listId,
          ViewerConstants.LISTS_FACETS_PARAMETERS_PROPERTY, parameterName,
          ViewerConstants.LISTS_FACETS_PARAMETERS_TYPE_PROPERTY);

        FacetParameter parameter = null;
        if (SimpleFacetParameter.class.getSimpleName().equalsIgnoreCase(type)) {
          parameter = buildSimpleFacetParameter(listId, parameterName);
        }

        if (parameter != null) {
          parameters.put(parameter.getName(), parameter);
        } else {
          logger.error("ConfigurationManager: ignoring FacetParameter '" + parameterName + "' of type '" + type
            + "' which has an invalid type or invalid set of args.");
        }
      }

      return parameters;
    }

    private static SimpleFacetParameter buildSimpleFacetParameter(String listId, String parameterName) {
      String name = buildNameArg(listId, parameterName);
      Integer limit = buildLimitArg(listId, parameterName);
      FacetParameter.SORT sort = buildSortArg(listId, parameterName);
      Integer minCount = buildMinCountArg(listId, parameterName);

      // values is always not null, not need to check it
      List<String> values = buildValuesArg(listId, parameterName);

      // check which arguments are not null and use the appropriate constructor
      if (name != null && limit != null && sort != null) {
        return new SimpleFacetParameter(name, limit, sort);
      } else if (name != null && sort != null) {
        return new SimpleFacetParameter(name, sort);
      } else if (name != null && minCount != null && limit != null) {
        return new SimpleFacetParameter(name, values, minCount, limit);
      } else if (name != null && limit != null) {
        return new SimpleFacetParameter(name, limit);
      } else if (name != null && minCount != null) {
        return new SimpleFacetParameter(name, values, minCount);
      } else if (name != null) {
        return new SimpleFacetParameter(name, values);
      } else {
        // Assuming a FacetParameter is useless without a name
        return null;
      }
    }

    private static String buildNameArg(String listId, String parameterName) {
      return parameterName;
    }

    private static String buildStartArg(String listId, String parameterName) {
      return getString(ViewerConstants.LISTS_PROPERTY, listId, ViewerConstants.LISTS_FACETS_PARAMETERS_PROPERTY,
        parameterName, ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_START_PROPERTY);
    }

    private static String buildEndArg(String listId, String parameterName) {
      return getString(ViewerConstants.LISTS_PROPERTY, listId, ViewerConstants.LISTS_FACETS_PARAMETERS_PROPERTY,
        parameterName, ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_END_PROPERTY);
    }

    private static String buildGapArg(String listId, String parameterName) {
      return getString(ViewerConstants.LISTS_PROPERTY, listId, ViewerConstants.LISTS_FACETS_PARAMETERS_PROPERTY,
        parameterName, ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_GAP_PROPERTY);
    }

    private static Integer buildLimitArg(String listId, String parameterName) {
      return getInt(ViewerConstants.LISTS_PROPERTY, listId, ViewerConstants.LISTS_FACETS_PARAMETERS_PROPERTY,
        parameterName, ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_LIMIT_PROPERTY);
    }

    private static FacetParameter.SORT buildSortArg(String listId, String parameterName) {
      String possibleSort = getString(ViewerConstants.LISTS_PROPERTY, listId,
        ViewerConstants.LISTS_FACETS_PARAMETERS_PROPERTY, parameterName,
        ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_SORT_PROPERTY);

      if (possibleSort != null) {
        for (FacetParameter.SORT sort : FacetParameter.SORT.values()) {
          if (possibleSort.equalsIgnoreCase(sort.name())) {
            return sort;
          }
        }
        logger.error("ConfigurationManager: list '" + listId + "', parameter '" + parameterName
          + "' has an invalid SORT argument: '" + possibleSort + "'.");
      }

      return null;
    }

    private static List<String> buildValuesArg(String listId, String parameterName) {
      return getStringList(ViewerConstants.LISTS_PROPERTY, listId, ViewerConstants.LISTS_FACETS_PARAMETERS_PROPERTY,
        parameterName, ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_VALUES_PROPERTY);
    }

    private static Integer buildMinCountArg(String listId, String parameterName) {
      return getInt(ViewerConstants.LISTS_PROPERTY, listId, ViewerConstants.LISTS_FACETS_PARAMETERS_PROPERTY,
        parameterName, ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_PROPERTY,
        ViewerConstants.LISTS_FACETS_PARAMETERS_ARGS_MINCOUNT_PROPERTY);
    }
  }
}
