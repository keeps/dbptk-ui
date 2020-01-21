package com.databasepreservation.common.client.tools;

import java.util.List;
import java.util.Map;

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
