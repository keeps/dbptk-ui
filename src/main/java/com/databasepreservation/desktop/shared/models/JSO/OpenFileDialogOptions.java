package com.databasepreservation.desktop.shared.models.JSO;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class OpenFileDialogOptions extends JavaScriptObject {


  protected OpenFileDialogOptions() {
  }

  // [{name: 'Images', extensions: ['jpg', 'png', 'gif'] }

  public static native OpenFileDialogOptions create(JsArrayString properties, JsArray<FilterJSO> filters) /*-{
      return {properties: properties, filters: filters};
  }-*/;
}
