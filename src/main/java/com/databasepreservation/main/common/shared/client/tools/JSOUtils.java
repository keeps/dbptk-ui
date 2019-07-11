package com.databasepreservation.main.common.shared.client.tools;

import com.databasepreservation.main.desktop.shared.models.Filter;
import com.databasepreservation.main.desktop.shared.models.JSO.FilterJSO;
import com.databasepreservation.main.desktop.shared.models.JSO.OpenFileDialogOptions;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class JSOUtils {

  public static JavaScriptObject getOpenDialogOptions(List<String> propertiesList, List<Filter> filtersList) {

    JsArrayString properties = JavaScriptObject.createArray().cast();
    for (String property : propertiesList) {
      properties.push(property);
    }

    JsArray<FilterJSO> filters = JavaScriptObject.createArray().cast();

    for (Filter filter : filtersList) {
      JsArrayString extensions = JavaScriptObject.createArray().cast();
      for (String extension : filter.getExtensions()) {
        extensions.push(extension);
      }

      FilterJSO filterJSO = FilterJSO.create(filter.getName(), extensions);

      filters.push(filterJSO);
    }

    return OpenFileDialogOptions.create(properties, filters);
  }
}
