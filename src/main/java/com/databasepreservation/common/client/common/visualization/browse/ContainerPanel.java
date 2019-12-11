package com.databasepreservation.common.client.common.visualization.browse;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.services.AuthenticationService;
import com.databasepreservation.common.utils.UserUtility;
import org.roda.core.data.v2.user.User;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.UserLogin;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.ContentPanelLoader;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.index.IsIndexed;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerDatabaseStatus;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
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
    return instances.computeIfAbsent(databaseUUID, k -> new ContainerPanel(databaseUUID, initMenu));
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
  FlowPanel toplevel;

  @UiField
  FlowPanel toolbar;

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
    UserLogin.getInstance().addLoginStatusListener(this::buildMenuForUser);

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

    AuthenticationService.Util.call((Boolean authenticationIsEnabled) -> {
      if (authenticationIsEnabled) {
        if (user.isGuest()) {
          menu.addItem(FontAwesomeIconManager.loaded(FontAwesomeIconManager.USER, messages.loginLogin()),
            (Command) () -> UserLogin.getInstance().login());
        } else {
          if (!hideMenu) {
            MenuBar subMenu = new MenuBar(true);
            subMenu.addItem(messages.loginLogout(), (Command) () -> UserLogin.getInstance().logout());
            menu.addItem(FontAwesomeIconManager.loaded(FontAwesomeIconManager.USER, user.getFullName()), subMenu);
            if (user.getAllRoles().contains("administrators")) {
              menu.addItem(FontAwesomeIconManager.loaded(FontAwesomeIconManager.NEW_UPLOAD, messages.newUpload()),
                  (Command) HistoryManager::gotoNewUpload);
            }
            menu.addItem(
                FontAwesomeIconManager.loaded(FontAwesomeIconManager.DATABASES, messages.menusidebar_manageDatabases()),
                (Command) HistoryManager::gotoDatabaseList);
          }
        }
      } else {
        menu.addItem(FontAwesomeIconManager.loaded(FontAwesomeIconManager.NEW_UPLOAD, messages.newUpload()),
          (Command) HistoryManager::gotoNewUpload);
        menu.addItem(
            FontAwesomeIconManager.loaded(FontAwesomeIconManager.DATABASES, messages.menusidebar_manageDatabases()),
          (Command) HistoryManager::gotoDatabaseList);
      }

      if (!hideMenu) {
        MenuBar languagesMenu = new MenuBar(true);

        setLanguageMenu(languagesMenu);

        MenuItem languagesMenuItem = new MenuItem(
            FontAwesomeIconManager.loaded(FontAwesomeIconManager.GLOBE, selectedLanguage), languagesMenu);
        languagesMenuItem.addStyleName("menu-item menu-item-label menu-item-language");
        menu.addItem(languagesMenuItem);
      }
    }).isAuthenticationEnabled();
  }

  private void setLanguageMenu(MenuBar languagesMenu) {
    String locale = LocaleInfo.getCurrentLocale().getLocaleName();

    // Getting supported languages and their display name
    Map<String, String> supportedLanguages = new HashMap<>();

    for (String localeName : LocaleInfo.getAvailableLocaleNames()) {
      if (!"default".equals(localeName)) {
        supportedLanguages.put(localeName, LocaleInfo.getLocaleNativeDisplayName(localeName));
      }
    }

    languagesMenu.clearItems();

    supportedLanguages.keySet().forEach(key -> {
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
            () -> JavascriptUtils.changeLocale(key));
        languagesMenu.addItem(languageMenuItem);
        languageMenuItem.addStyleName("menu-item-language");
      }
    });
  }

  public void load(ContentPanel panel) {
    panel.handleBreadcrumb(breadcrumb);
    panelContainer.setWidget(panel);
    panel.setVisible(true);
  }

  public void load(ContentPanelLoader panelLoader) {
    if (databaseUUID != null && (database == null || !ViewerDatabaseStatus.AVAILABLE.equals(database.getStatus()))) {
      // need to load database (not present or not available), go get it
      loadPanelWithDatabase(panelLoader);
    } else {
      loadPanel(panelLoader);
    }
  }

  private void loadPanelWithDatabase(final ContentPanelLoader panelLoader) {
    DatabaseService.Util.call((IsIndexed result) -> {
      database = (ViewerDatabase) result;
      loadPanel(panelLoader);
    }).retrieve(databaseUUID, databaseUUID);
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
