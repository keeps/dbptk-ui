/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.tools;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.databasepreservation.common.client.models.JSO.ExtensionFilter;
import com.databasepreservation.common.client.models.JSO.FilterJSO;
import com.databasepreservation.common.client.models.JSO.OpenFileDialogOptions;
import com.databasepreservation.common.client.models.structure.ViewerCell;
import com.databasepreservation.common.client.models.structure.ViewerTable;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JSOUtils {
  //private static final Logger LOGGER = LoggerFactory.getLogger(JSOUtils.class);

  public static JavaScriptObject getOpenDialogOptions(List<String> propertiesList, List<ExtensionFilter> filtersList) {

    JsArrayString properties = JavaScriptObject.createArray().cast();
    for (String property : propertiesList) {
      properties.push(property);
    }

    JsArray<FilterJSO> filters = JavaScriptObject.createArray().cast();

    for (ExtensionFilter extensionFilter : filtersList) {
      JsArrayString extensions = JavaScriptObject.createArray().cast();
      for (String extension : extensionFilter.getExtensions()) {
        extensions.push(extension);
      }

      FilterJSO filterJSO = FilterJSO.create(extensionFilter.getName(), extensions);

      filters.push(filterJSO);
    }

    return OpenFileDialogOptions.create(properties, filters);
  }

  public static String cellsToJson(String... parameters) {
    String json = "";

    if ((parameters.length % 2) != 0) {
     // LOGGER.warn("transform cells to JSON failed because parameters array must have pairs of elements (even length)");
      return json;
    } else {
      JSONObject jsonObj = new JSONObject();
      for (int i = 0; i < parameters.length; i+=2) {
        jsonObj.put(parameters[i], new JSONString(parameters[i+1]));
      }
      return jsonObj.toString();
    }
  }

  public static String cellsToJson(Map<String, ViewerCell> map, ViewerTable nestedTable) {
    String json = "";
    if (map != null && !map.isEmpty()) {
      JSONObject jsonObj = new JSONObject();
      for (Map.Entry<String, ViewerCell> entry : map.entrySet()) {
        jsonObj.put(nestedTable.getColumnBySolrName(entry.getKey()).getDisplayName(),
          new JSONString(entry.getValue().getValue()));
      }
      json = jsonObj.toString();
    }
    return json;
  }
}
