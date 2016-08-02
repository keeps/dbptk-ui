package com.databasepreservation.visualization.client.main;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.databasepreservation.visualization.shared.client.Tools.FontAwesomeIconManager;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.databasepreservation.visualization.shared.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class MainPanel extends Composite {
  // databaseUUID, databaseName
  private static Map<String, String> databaseNames = new HashMap<>();

  interface MainPanelUiBinder extends UiBinder<Widget, MainPanel> {
  }

  @UiField
  SimplePanel contentPanel;

  @UiField
  AccessibleFocusPanel homeLinkArea;

  @UiField
  SimplePanel bannerLogo;

  private static MainPanelUiBinder binder = GWT.create(MainPanelUiBinder.class);

  public MainPanel() {
    initWidget(binder.createAndBindUi(this));
    reSetHeader();
  }

  public void reSetHeader(final String databaseUUID) {
    if (databaseUUID == null) {
      reSetHeader();
    } else if (databaseNames.containsKey(databaseUUID)) {
      reSetHeader(databaseUUID, databaseNames.get(databaseUUID));
    } else {
      reSetHeader();

      BrowserService.Util.getInstance().retrieve(ViewerDatabase.class.getName(), databaseUUID,
        new DefaultAsyncCallback<IsIndexed>() {
          @Override
          public void onSuccess(IsIndexed result) {
            ViewerDatabase database = (ViewerDatabase) result;
            String databaseName = database.getMetadata().getName();

            databaseNames.put(databaseUUID, databaseName);
            reSetHeader(databaseUUID, databaseName);
          }
        });
    }
  }

  private void reSetHeader() {
    HTMLPanel headerText = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.DATABASE) + " Database Visualization Toolkit"));
    headerText.addStyleName("homeText");
    bannerLogo.setWidget(headerText);

    homeLinkArea.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        HistoryManager.gotoRoot();
      }
    });

    homeLinkArea.setTitle("Go home");
  }

  private void reSetHeader(final String databaseUUID, String databaseName) {
    HTMLPanel headerText = new HTMLPanel(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager
      .getTag(FontAwesomeIconManager.DATABASE) + " " + SafeHtmlUtils.htmlEscape(databaseName)));
    headerText.addStyleName("homeText");
    bannerLogo.setWidget(headerText);

    homeLinkArea.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        HistoryManager.gotoDatabase(databaseUUID);
      }
    });

    homeLinkArea.setTitle("Go to database information");
  }
}
