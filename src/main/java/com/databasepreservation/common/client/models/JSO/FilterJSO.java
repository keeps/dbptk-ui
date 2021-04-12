/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.JSO;

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
