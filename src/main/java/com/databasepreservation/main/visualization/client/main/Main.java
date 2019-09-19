package com.databasepreservation.main.visualization.client.main;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.client.ClientLogger;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Main implements EntryPoint {
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private MainPanel mainPanel;
  private Footer footer;

  /**
   * Create a new main
   */
  public Main() {
    mainPanel = new MainPanel();
    footer = new Footer();
  }

  /**
   * The entry point method, called automatically by loading a module that
   * declares an implementing class as an entry point.
   */
  @Override
  public void onModuleLoad() {
    BrowserService.Util.getInstance().getApplicationType(new DefaultAsyncCallback<String>() {

      @Override
      public void onSuccess(String result) {
        ApplicationType.setType(result);
      }
    });

    // Set uncaught exception handler
    ClientLogger.setUncaughtExceptionHandler();

    // Remove loading image
    RootPanel.getBodyElement().removeChild(DOM.getElementById("loading"));

    RootPanel.get().add(mainPanel);
    RootPanel.get().add(footer);
    RootPanel.get().addStyleName("roda");

    mainPanel.onHistoryChanged(History.getToken());
    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        mainPanel.onHistoryChanged(event.getValue());
      }
    });
  }
}
