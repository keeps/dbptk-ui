package com.databasepreservation.common.shared.models;

import java.util.ArrayList;
import java.util.List;

import com.databasepreservation.common.shared.ViewerConstants;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class Filter {

  private String name;
  private List<String> extensions = null;

  public Filter() {}

  public Filter(String name, List<String> extensions) {
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

  public Filter createFilterTypeFromDBPTK(String type) {
    if (extensions == null) {
      extensions = new ArrayList<>();
    }
    switch (type) {
      case "XML_EXTENSION":
        this.name = "XML";
        this.extensions.add("xml");
        return this;
      default:
        this.name = ViewerConstants.SIARD_FILES;
        this.extensions.add(ViewerConstants.SIARD);
        return this;
    }
  }
}
