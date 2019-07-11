package com.databasepreservation.main.desktop.shared.models;

import java.util.List;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class Filter {

  private String name;
  private List<String> extensions;

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
}
