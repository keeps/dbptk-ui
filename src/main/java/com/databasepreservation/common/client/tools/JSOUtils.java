package com.databasepreservation.common.client.tools;

import java.util.List;

import com.databasepreservation.common.client.models.JSO.ExtensionFilter;
import com.databasepreservation.common.client.models.JSO.FilterJSO;
import com.databasepreservation.common.client.models.JSO.OpenFileDialogOptions;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

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
}
