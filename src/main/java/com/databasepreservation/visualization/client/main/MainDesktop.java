package com.databasepreservation.visualization.client.main;

import com.databasepreservation.visualization.shared.client.ClientLogger;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.*;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class MainDesktop implements EntryPoint {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  private HomePage homePage;

  /**
   * Create a new main
   */
  public MainDesktop() {
    homePage = new HomePage();
  }

  @Override
  public void onModuleLoad() {

    // Set uncaught exception handler
    ClientLogger.setUncaughtExceptionHandler();

    // Remove loading image
    RootPanel.getBodyElement().removeChild(DOM.getElementById("loading"));

    RootPanel.get().add(homePage);

    homePage.onHistoryChanged(History.getToken());
    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        homePage.onHistoryChanged(event.getValue());
      }
    });

  }
}
