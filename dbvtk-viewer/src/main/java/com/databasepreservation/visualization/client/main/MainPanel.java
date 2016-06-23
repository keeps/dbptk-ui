package com.databasepreservation.visualization.client.main;

import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.databasepreservation.visualization.shared.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class MainPanel extends Composite {
  interface MainPanelUiBinder extends UiBinder<Widget, MainPanel> {
  }

  @UiField
  SimplePanel contentPanel;

  @UiField
  AccessibleFocusPanel homeLinkArea;

  @UiField
  FlowPanel bannerLogo;

  private static MainPanelUiBinder binder = GWT.create(MainPanelUiBinder.class);

  public MainPanel() {
    initWidget(binder.createAndBindUi(this));

    bannerLogo.add(new HTMLPanel(new SafeHtml() {
      @Override
      public String asString() {
        return "<div class=\"homeText\">Database Viewer <span style=\"font-size:0.5em\">(preview release)</span></div>";
      }
    }));

    homeLinkArea.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        HistoryManager.gotoRoot();
      }
    });

    homeLinkArea.setTitle("Go home");
  }
}
