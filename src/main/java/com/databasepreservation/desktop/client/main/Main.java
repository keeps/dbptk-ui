/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.desktop.client.main;

import com.databasepreservation.common.client.ClientLogger;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.services.ContextService;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class Main implements EntryPoint {

  private ClientLogger logger = new ClientLogger(getClass().getName());
  private MainPanelDesktop mainPanel;

  /**
   * Create a new main
   */
  public Main() {
    mainPanel = new MainPanelDesktop();
  }

  @Override
  public void onModuleLoad() {
    ContextService.Util.call((String env) -> {
      ApplicationType.setType(env);
      // Set uncaught exception handler
      ClientLogger.setUncaughtExceptionHandler();

      // Remove loading image
      RootPanel.getBodyElement().removeChild(DOM.getElementById("loading"));

      RootPanel.get().add(mainPanel);
      mainPanel.onHistoryChanged(History.getToken());
      History.addValueChangeHandler(event -> mainPanel.onHistoryChanged(event.getValue()));

    }).getEnvironment();
  }
}
