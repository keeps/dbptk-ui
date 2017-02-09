package com.databasepreservation.visualization.client.browse;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.user.User;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.databasepreservation.visualization.client.common.LoginStatusListener;
import com.databasepreservation.visualization.client.common.UserLogin;
import com.databasepreservation.visualization.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.visualization.client.common.utils.RightPanelLoader;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.FontAwesomeIconManager;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabasePanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static Map<String, DatabasePanel> instances = new HashMap<>();

  public static DatabasePanel getInstance(String databaseUUID) {
    String code = databaseUUID;

    DatabasePanel instance = instances.get(code);
    if (instance == null) {
      instance = new DatabasePanel(databaseUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, DatabasePanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  DatabaseSidebar sidebar;

  @UiField
  SimplePanel rightPanelContainer;

  @UiField
  MenuBar menu;

  private String databaseUUID;
  private ViewerDatabase database = null;

  private DatabasePanel(String databaseUUID) {
    this.databaseUUID = databaseUUID;
    this.sidebar = DatabaseSidebar.getInstance(databaseUUID);

    initWidget(uiBinder.createAndBindUi(this));

    initMenu();

    if (databaseUUID == null) {
      BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forDatabases());
    } else {
      BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.loadingDatabase(databaseUUID));
    }
  }

  private void initMenu() {
    UserLogin.getInstance().addLoginStatusListener(new LoginStatusListener() {
      @Override
      public void onLoginStatusChanged(User user) {
        buildMenuForUser(user);
      }
    });

    UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
      @Override
      public void onSuccess(User user) {
        buildMenuForUser(user);
      }
    });
  }

  private void buildMenuForUser(final User user) {
    menu.clearItems();
    BrowserService.Util.getInstance().isAuthenticationEnabled(new DefaultAsyncCallback<Boolean>() {
      @Override
      public void onSuccess(Boolean authenticationIsEnabled) {
        if (authenticationIsEnabled) {
          if (user.isGuest()) {
            menu.addItem(FontAwesomeIconManager.loaded(FontAwesomeIconManager.USER, messages.loginLogin()),
              new Command() {
                @Override
                public void execute() {
                  UserLogin.getInstance().login();
                }
              });
          } else {
            MenuBar subMenu = new MenuBar(true);
            subMenu.addItem(messages.loginLogout(), new Command() {
              @Override
              public void execute() {
                UserLogin.getInstance().logout();
              }
            });
            menu.addItem(FontAwesomeIconManager.loaded(FontAwesomeIconManager.USER, user.getFullName()), subMenu);
          }
        } else {
          menu.addItem(
            FontAwesomeIconManager.loaded(FontAwesomeIconManager.DATABASES, messages.menusidebar_manageDatabases()),
            new Command() {
              @Override
              public void execute() {
                HistoryManager.gotoDatabaseList();
              }
            });
        }
      }
    });
  }

  public void load(RightPanelLoader rightPanelLoader) {
    if (databaseUUID != null && database == null) {
      // need to load database, go get it
      loadPanelWithDatabase(rightPanelLoader);
    } else {
      loadPanel(rightPanelLoader);
    }
  }

  private void loadPanelWithDatabase(final RightPanelLoader rightPanelLoader) {
    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          sidebar.init(database);
          loadPanel(rightPanelLoader);
        }
      });
  }

  private void loadPanel(RightPanelLoader rightPanelLoader) {
    RightPanel rightPanel = rightPanelLoader.load(database);

    if (rightPanel != null) {
      rightPanel.handleBreadcrumb(breadcrumb);
      rightPanelContainer.setWidget(rightPanel);
      rightPanel.setVisible(true);
    }
  }
}
