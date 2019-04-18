package com.databasepreservation.visualization.client.browse;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.visualization.shared.ViewerStructure.IsIndexed;
import org.roda.core.data.v2.user.User;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.common.DefaultAsyncCallback;
import com.databasepreservation.visualization.client.common.LoginStatusListener;
import com.databasepreservation.visualization.client.common.UserLogin;
import com.databasepreservation.visualization.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.visualization.client.common.utils.JavascriptUtils;
import com.databasepreservation.visualization.client.common.utils.RightPanelLoader;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.FontAwesomeIconManager;
import com.databasepreservation.visualization.shared.client.Tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
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
  private String selectedLanguage;

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
    menu.addStyleName("user-menu");
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

    final boolean hideMenu = Window.Location.getHref().contains("branding=false");

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
            if (!hideMenu) {
              MenuBar subMenu = new MenuBar(true);
              subMenu.addItem(messages.loginLogout(), new Command() {
                @Override
                public void execute() {
                  UserLogin.getInstance().logout();
                }
              });
              menu.addItem(FontAwesomeIconManager.loaded(FontAwesomeIconManager.USER, user.getFullName()), subMenu);
            }
          }
        } else {
          menu.addItem(FontAwesomeIconManager.loaded(FontAwesomeIconManager.NEW_UPLOAD, messages.newUpload()),
            new Command() {
              @Override
              public void execute() {
                HistoryManager.gotoNewUpload();
              }
            });
          menu.addItem(
            FontAwesomeIconManager.loaded(FontAwesomeIconManager.DATABASES, messages.menusidebar_manageDatabases()),
            new Command() {
              @Override
              public void execute() {
                HistoryManager.gotoDatabaseList();
              }
            });
        }

        if (!hideMenu) {
          MenuBar languagesMenu = new MenuBar(true);

          setLanguageMenu(languagesMenu);

          MenuItem languagesMenuItem = new MenuItem(
            FontAwesomeIconManager.loaded(FontAwesomeIconManager.GLOBE, selectedLanguage), languagesMenu);
          languagesMenuItem.addStyleName("menu-item menu-item-label menu-item-language");
          menu.addItem(languagesMenuItem);
        }
      }
    });
  }

  private void setLanguageMenu(MenuBar languagesMenu) {
    String locale = LocaleInfo.getCurrentLocale().getLocaleName();

    // Getting supported languages and their display name
    Map<String, String> supportedLanguages = new HashMap<String, String>();

    for (String localeName : LocaleInfo.getAvailableLocaleNames()) {
      if (!"default".equals(localeName)) {
        supportedLanguages.put(localeName, LocaleInfo.getLocaleNativeDisplayName(localeName));
      }
    }

    languagesMenu.clearItems();

    for (final String key : supportedLanguages.keySet()) {
      if (key.equals(locale)) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        String iconHTML = "<i class='fa fa-check'></i>";

        b.append(SafeHtmlUtils.fromSafeConstant(supportedLanguages.get(key)));
        b.append(SafeHtmlUtils.fromSafeConstant(iconHTML));

        MenuItem languageMenuItem = new MenuItem(b.toSafeHtml());
        languageMenuItem.addStyleName("menu-item-language-selected");
        languageMenuItem.addStyleName("menu-item-language");
        languagesMenu.addItem(languageMenuItem);
        selectedLanguage = supportedLanguages.get(key);
      } else {
        MenuItem languageMenuItem = new MenuItem(SafeHtmlUtils.fromSafeConstant(supportedLanguages.get(key)),
          new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
              JavascriptUtils.changeLocale(key);
            }
          });
        languagesMenu.addItem(languageMenuItem);
        languageMenuItem.addStyleName("menu-item-language");
      }
    }
  }

  public void load(RightPanelLoader rightPanelLoader) {
    GWT.log("load. uuid: " + databaseUUID + ", database: " + database);
    if (databaseUUID != null && (database == null || !ViewerDatabase.Status.AVAILABLE.equals(database.getStatus()))) {
      // need to load database (not present or not available), go get it
      GWT.log("getting db");
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
          loadPanel(rightPanelLoader);
        }
      });
  }

  private void loadPanel(RightPanelLoader rightPanelLoader) {
    GWT.log("have db: " + database + "sb.init: " + sidebar.isInitialized());
    RightPanel rightPanel = rightPanelLoader.load(database);

    if (database != null && !sidebar.isInitialized()) {
      sidebar.init(database);
    }

    if (rightPanel != null) {
      rightPanel.handleBreadcrumb(breadcrumb);
      rightPanelContainer.setWidget(rightPanel);
      rightPanel.setVisible(true);
    }
  }
}
