package com.databasepreservation.desktop.shared.models.JSO;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class FilterJSO extends JavaScriptObject {

  protected FilterJSO() {
  }

  public static native FilterJSO create(String name, JsArrayString extensions) /*-{
    return {name : name, extensions: extensions};
  }-*/;
}
