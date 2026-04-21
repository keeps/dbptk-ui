package com.databasepreservation.common.client.common.visualization.browse.configuration.columns.helpers;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TabPanel;

public class TabbedColumnOptionsPanel extends ColumnOptionsPanel implements ValidatableOptionsPanel {

  private final TabPanel tabPanel = new TabPanel();
  private final List<ColumnOptionsPanel> panels = new ArrayList<>();

  public TabbedColumnOptionsPanel() {
    FlowPanel wrapper = new FlowPanel();
    wrapper.addStyleName("metadata-edit column-config-dialog");
    tabPanel.addStyleName("browseItemMetadata metadata-edit-tab");
    wrapper.add(tabPanel);
    initWidget(wrapper);
  }

  public void addTab(String title, ColumnOptionsPanel panel) {
    panels.add(panel);
    tabPanel.add(panel, title);
    if (tabPanel.getWidgetCount() == 1)
      tabPanel.selectTab(0);
  }

  public List<ColumnOptionsPanel> getPanels() {
    return panels;
  }

  @Override
  public boolean validate() {
    boolean allPanelsValid = true;
    int firstInvalidTabIndex = -1;

    for (int i = 0; i < panels.size(); i++) {
      ColumnOptionsPanel panel = panels.get(i);
      if (panel instanceof ValidatableOptionsPanel && !((ValidatableOptionsPanel) panel).validate()) {
        allPanelsValid = false;
        if (firstInvalidTabIndex == -1)
          firstInvalidTabIndex = i;
      }
    }

    // UX: Auto-focus the tab containing the error
    if (!allPanelsValid && firstInvalidTabIndex != -1) {
      tabPanel.selectTab(firstInvalidTabIndex);
    }
    return allPanelsValid;
  }
}
