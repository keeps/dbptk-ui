/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.JSO;

import com.databasepreservation.common.client.ViewerConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ExtensionFilter {

  private String name;
  private List<String> extensions = null;

  public ExtensionFilter() {}

  public ExtensionFilter(String name, List<String> extensions) {
    this.name = name;
    this.extensions = extensions;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getExtensions() {
    return extensions;
  }

  public void setExtensions(List<String> extensions) {
    this.extensions = extensions;
  }

  public ExtensionFilter createFilterTypeFromDBPTK(String type) {
    if (extensions == null) {
      extensions = new ArrayList<>();
    }
    switch (type) {
      case "XML_EXTENSION":
        this.name = "XML";
        this.extensions.add("xml");
        return this;
      case "JSON_EXTENSION":
        this.name = "JSON";
        this.extensions.add("json");
        return this;
      default:
        this.name = ViewerConstants.SIARD_FILES;
        this.extensions.add(ViewerConstants.SIARD);
        return this;
    }
  }
}
