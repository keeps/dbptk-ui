package com.databasepreservation.common.shared.client.common.sidebar;

import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.ViewerStructure.ViewerMetadata;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class DesktopDatabaseSidebar extends DatabaseSidebarAbstract {
  /**
   * Creates a new DatabaseSidebar, rarely hitting the database more than once for
   * each database.
   *
   * @param databaseUUID
   *          the database UUID
   * @return a DatabaseSidebar instance
   */
  public static DatabaseSidebarAbstract getInstance(String databaseUUID) {
    String code = databaseUUID;

    if (code == null) {
      return getEmptyInstance();
    }

    DatabaseSidebarAbstract instance = instances.get(code);
    if (instance == null || instance.database == null
      || !ViewerDatabase.Status.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new DesktopDatabaseSidebar(databaseUUID);
      instances.put(code, instance);
    } else {
      // workaround because the same DatabaseSidebar can not belong to multiple
      // widgets
      return new DesktopDatabaseSidebar(instance);
    }
    return instance;
  }

  /**
   * Creates a new DatabaseSidebar, rarely hitting the database more than once for
   * each database.
   *
   * @param database
   *          the database
   * @return a DatabaseSidebar instance
   */
  public static DatabaseSidebarAbstract getInstance(ViewerDatabase database) {
    if (database == null) {
      return getEmptyInstance();
    }

    DatabaseSidebarAbstract instance = instances.get(database.getUUID());
    if (instance == null || instance.database == null
      || !ViewerDatabase.Status.AVAILABLE.equals(instance.database.getStatus())) {
      instance = new DesktopDatabaseSidebar(database);
      instances.put(database.getUUID(), instance);
    } else {
      // workaround because the same DatabaseSidebar can not belong to multiple
      // widgets
      return new DesktopDatabaseSidebar(instance);
    }
    return instance;
  }

  /**
   * Creates a new (dummy) DatabaseSidebar that is not visible. This method exists
   * so that pages can opt for not using a sidebar at all.
   *
   * @return a new invisible DatabaseSidebar
   */
  public static DatabaseSidebarAbstract getEmptyInstance() {
    return new DesktopDatabaseSidebar();
  }

  /**
   * Clone constructor, because the same DatabaseSidebar can not be child in more
   * than one widget
   *
   * @param other
   *          the DatabaseSidebar used in another widget
   */
  private DesktopDatabaseSidebar(DatabaseSidebarAbstract other) {
    initialized = other.initialized;
    initWidget(uiBinder.createAndBindUi(this));
    searchInputBox.setText(other.searchInputBox.getText());
    init(other.database);
  }

  /**
   * Use DatabaseSidebar.getInstance to obtain an instance
   */
  private DesktopDatabaseSidebar(ViewerDatabase database) {
    initWidget(uiBinder.createAndBindUi(this));
    init(database);
  }

  /**
   * Empty constructor, for pages that do not have a sidebar
   */
  private DesktopDatabaseSidebar() {
    initWidget(uiBinder.createAndBindUi(this));
    this.setVisible(false);
  }

  /**
   * Use DatabaseSidebar.getInstance to obtain an instance
   */
  private DesktopDatabaseSidebar(String databaseUUID) {
    this();
    this.databaseUUID = databaseUUID;
  }

  @Override
  public void init() {
    // database metadata
    final ViewerMetadata metadata = database.getMetadata();

    sidebarGroup.add(
      new SidebarItem(messages.menusidebar_database()).addIcon(FontAwesomeIconManager.DATABASE).setH5().setIndent0());

    sidebarGroup.add(
      new SidebarHyperlink(messages.menusidebar_information(), HistoryManager.linkToDatabaseDesktop(database.getUUID()))
        .addIcon(FontAwesomeIconManager.DATABASE_INFORMATION).setH6().setIndent1());

    sidebarGroup
      .add(new SidebarHyperlink(messages.titleReport(), HistoryManager.linkToDesktopDatabaseReport(database.getUUID()))
        .addIcon(FontAwesomeIconManager.DATABASE_REPORT).setH6().setIndent1());

    sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_usersRoles(),
      HistoryManager.linkToDesktopDatabaseUsers(database.getUUID())).addIcon(FontAwesomeIconManager.DATABASE_USERS)
        .setH6().setIndent1());

    sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_savedSearches(),
      HistoryManager.linkToSavedSearches(database.getUUID())).addIcon(FontAwesomeIconManager.SAVED_SEARCH)
        .setH6().setIndent1());

    sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_searchAllRecords(),
      HistoryManager.linkToDesktopDatabaseSearch(database.getUUID())).addIcon(FontAwesomeIconManager.DATABASE_SEARCH)
        .setH6().setIndent1());

    for (final ViewerSchema schema : metadata.getSchemas()) {
      sidebarGroup.add(new SidebarItem(schema.getName()).addIcon(FontAwesomeIconManager.SCHEMA).setH5().setIndent0());

      sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_structure(),
        HistoryManager.linkToDesktopSchemaStructure(database.getUUID(), schema.getUUID()))
          .addIcon(FontAwesomeIconManager.SCHEMA_STRUCTURE).setH6().setIndent1());

      sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_routines(),
        HistoryManager.linkToDesktopSchemaRoutines(database.getUUID(), schema.getUUID()))
          .addIcon(FontAwesomeIconManager.SCHEMA_ROUTINES).setH6().setIndent1());

      sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_triggers(),
        HistoryManager.linkToDesktopSchemaTriggers(database.getUUID(), schema.getUUID()))
          .addIcon(FontAwesomeIconManager.SCHEMA_TRIGGERS).setH6().setIndent1());

      sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_checkConstraints(),
        HistoryManager.linkToDesktopSchemaCheckConstraints(database.getUUID(), schema.getUUID()))
          .addIcon(FontAwesomeIconManager.SCHEMA_CHECK_CONSTRAINTS).setH6().setIndent1());

      sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_views(),
        HistoryManager.linkToDesktopSchemaViews(database.getUUID(), schema.getUUID()))
          .addIcon(FontAwesomeIconManager.SCHEMA_VIEWS).setH6().setIndent1());

      sidebarGroup.add(new SidebarHyperlink(messages.menusidebar_data(),
        HistoryManager.linkToDesktopSchemaData(database.getUUID(), schema.getUUID()))
          .addIcon(FontAwesomeIconManager.SCHEMA_DATA).setH6().setIndent1());

      for (ViewerTable table : schema.getTables()) {
        sidebarGroup
          .add(new SidebarHyperlink(table.getName(), HistoryManager.linkToDesktopTable(database.getUUID(), table.getUUID()))
            .addIcon(FontAwesomeIconManager.TABLE).setH6().setIndent2());
      }

      searchInit();
    }

    setVisible(true);
  }
}