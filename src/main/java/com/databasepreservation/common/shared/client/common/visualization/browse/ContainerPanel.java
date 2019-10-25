package com.databasepreservation.common.shared.client.common.visualization.browse;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.v2.user.User;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.IsIndexed;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.common.ContentPanel;
import com.databasepreservation.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.shared.client.common.LoginStatusListener;
import com.databasepreservation.common.shared.client.common.UserLogin;
import com.databasepreservation.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.common.shared.client.common.utils.ContentPanelLoader;
import com.databasepreservation.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ContainerPanel extends Composite {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static Map<String, ContainerPanel> instances = new HashMap<>();

  public static ContainerPanel getInstance(String databaseUUID, boolean initMenu) {
    String code = databaseUUID;

    ContainerPanel instance = instances.get(code);
    if (instance == null) {
      instance = new ContainerPanel(databaseUUID, initMenu);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, ContainerPanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField
  SimplePanel panelContainer;

  @UiField
  MenuBar menu;

  @UiField
  FlowPanel toplevel, toolbar;

  private String databaseUUID;
  private ViewerDatabase database = null;
  private String selectedLanguage;

  private ContainerPanel(String databaseUUID, boolean initMenu) {
    this.databaseUUID = databaseUUID;

    initWidget(uiBinder.createAndBindUi(this));
    if(ApplicationType.getType().equals(ViewerConstants.SERVER)){
      toolbar.getElement().addClassName("filePreviewToolbar");
    } else {
      toolbar.getElement().addClassName("desktopToolbar");
    }

    if (initMenu) {
      initMenu();
    }

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

  public void load(ContentPanel panel) {
    panel.handleBreadcrumb(breadcrumb);
    panelContainer.setWidget(panel);
    panel.setVisible(true);
  }

  public void load(ContentPanelLoader panelLoader) {
    if (databaseUUID != null && (database == null || !ViewerDatabase.Status.AVAILABLE.equals(database.getStatus()))) {
      // need to load database (not present or not available), go get it
      loadPanelWithDatabase(panelLoader);
    } else {
      loadPanel(panelLoader);
    }
  }

  private void loadPanelWithDatabase(final ContentPanelLoader panelLoader) {
    BrowserService.Util.getInstance().retrieve(databaseUUID, ViewerDatabase.class.getName(), databaseUUID,
      new DefaultAsyncCallback<IsIndexed>() {
        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          loadPanel(panelLoader);
        }
      });
  }

  public void loadPanel(ContentPanelLoader panelLoader) {
    ContentPanel panel = panelLoader.load(database);
    if (panel != null) {
      panel.handleBreadcrumb(breadcrumb);
      panelContainer.setWidget(panel);
      panel.setVisible(true);
    }
  }

  public void setTopLevelPanelCSS(String css) {
      toplevel.addStyleName(css);
  }
}
