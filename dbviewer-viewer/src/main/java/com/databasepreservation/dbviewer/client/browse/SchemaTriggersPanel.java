package com.databasepreservation.dbviewer.client.browse;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTrigger;
import com.databasepreservation.dbviewer.client.common.lists.BasicTablePanel;
import com.databasepreservation.dbviewer.client.common.search.SearchPanel;
import com.databasepreservation.dbviewer.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.dbviewer.client.common.utils.CommonClientUtils;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.dbviewer.shared.client.Tools.ViewerStringUtils;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SchemaTriggersPanel extends Composite {
  private static Map<String, SchemaTriggersPanel> instances = new HashMap<>();

  public static SchemaTriggersPanel getInstance(String databaseUUID, String schemaUUID) {
    String separator = "/";
    String code = databaseUUID + separator + schemaUUID;

    SchemaTriggersPanel instance = instances.get(code);
    if (instance == null) {
      instance = new SchemaTriggersPanel(databaseUUID, schemaUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, SchemaTriggersPanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerSchema schema;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  SearchPanel dbSearchPanel;

  @UiField(provided = true)
  DatabaseSidebar sidebar;
  @UiField
  FlowPanel contentItems;

  private SchemaTriggersPanel(final String databaseUUID, final String schemaUUID) {
    dbSearchPanel = new SearchPanel(new Filter(), "", "Search in all tables", false, false);
    sidebar = DatabaseSidebar.getInstance(databaseUUID);

    initWidget(uiBinder.createAndBindUi(this));

    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.loadingSchema(databaseUUID, schemaUUID));

    BrowserService.Util.getInstance().retrieve(ViewerDatabase.class.getName(), databaseUUID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          schema = database.getMetadata().getSchema(schemaUUID);
          init();
        }
      });
  }

  private void init() {
    // breadcrumb
    BreadcrumbManager.updateBreadcrumb(
      breadcrumb,
      BreadcrumbManager.forSchema(database.getMetadata().getName(), database.getUUID(), schema.getName(),
        schema.getUUID()));

    CommonClientUtils.addSchemaInfoToFlowPanel(contentItems, schema);

    boolean atLeastOneTrigger = false;
    for (ViewerTable viewerTable : schema.getTables()) {
      if (!viewerTable.getTriggers().isEmpty()) {
        atLeastOneTrigger = true;
        break;
      }
    }

    if (atLeastOneTrigger) {
      for (ViewerTable viewerTable : schema.getTables()) {
        if (!viewerTable.getTriggers().isEmpty()) {
          contentItems.add(getBasicTablePanelForTableTriggers(viewerTable));
        }
      }
    } else {
      Label noTriggers = new Label("This schema does not have any triggers.");
      noTriggers.addStyleName("strong");
      contentItems.add(noTriggers);
    }
  }

  private BasicTablePanel<ViewerTrigger> getBasicTablePanelForTableTriggers(final ViewerTable table) {
    Label header = new Label("Triggers in table " + table.getName());
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    return new BasicTablePanel<>(header, info, table.getTriggers().iterator(),

    new BasicTablePanel.ColumnInfo<>("Name", 15, new TextColumn<ViewerTrigger>() {
      @Override
      public String getValue(ViewerTrigger viewerTrigger) {
        return viewerTrigger.getName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Triggered action", 25, new Column<ViewerTrigger, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(ViewerTrigger viewerTrigger) {
        return SafeHtmlUtils.fromSafeConstant(SafeHtmlUtils.htmlEscape(
          viewerTrigger.getTriggeredAction().replace("\\u0020", " ")).replace("\\n", "<br/>"));
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Action time", 8, new TextColumn<ViewerTrigger>() {
      @Override
      public String getValue(ViewerTrigger viewerTrigger) {
        return viewerTrigger.getActionTime();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Trigger event", 8, new TextColumn<ViewerTrigger>() {
      @Override
      public String getValue(ViewerTrigger viewerTrigger) {
        return viewerTrigger.getTriggerEvent();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Alias list", 15, new TextColumn<ViewerTrigger>() {
      @Override
      public String getValue(ViewerTrigger viewerTrigger) {
        if (ViewerStringUtils.isNotBlank(viewerTrigger.getAliasList())) {
          return viewerTrigger.getAliasList();
        } else {
          return "";
        }
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Description", 35, new TextColumn<ViewerTrigger>() {
      @Override
      public String getValue(ViewerTrigger viewerTrigger) {
        if (ViewerStringUtils.isNotBlank(viewerTrigger.getDescription())) {
          return viewerTrigger.getDescription();
        } else {
          return "";
        }
      }
    })

    );
  }
}
