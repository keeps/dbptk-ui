package com.databasepreservation.visualization.client.browse;

import java.util.HashMap;
import java.util.Map;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.visualization.client.BrowserService;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerCheckConstraint;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerTable;
import com.databasepreservation.visualization.client.common.lists.BasicTablePanel;
import com.databasepreservation.visualization.client.common.search.SearchPanel;
import com.databasepreservation.visualization.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.visualization.client.common.utils.CommonClientUtils;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
public class SchemaCheckConstraintsPanel extends Composite {
  private static Map<String, SchemaCheckConstraintsPanel> instances = new HashMap<>();

  public static SchemaCheckConstraintsPanel getInstance(String databaseUUID, String schemaUUID) {
    String separator = "/";
    String code = databaseUUID + separator + schemaUUID;

    SchemaCheckConstraintsPanel instance = instances.get(code);
    if (instance == null) {
      instance = new SchemaCheckConstraintsPanel(databaseUUID, schemaUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, SchemaCheckConstraintsPanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerSchema schema;

  @UiField
  BreadcrumbPanel breadcrumb;

  @UiField(provided = true)
  DatabaseSidebar sidebar;
  @UiField
  FlowPanel contentItems;

  private SchemaCheckConstraintsPanel(final String databaseUUID, final String schemaUUID) {
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

    boolean atLeastOneConstraint = false;
    for (ViewerTable viewerTable : schema.getTables()) {
      if (!viewerTable.getCheckConstraints().isEmpty()) {
        atLeastOneConstraint = true;
        break;
      }
    }

    if (atLeastOneConstraint) {
      for (ViewerTable viewerTable : schema.getTables()) {
        if (!viewerTable.getCheckConstraints().isEmpty()) {
          contentItems.add(getBasicTablePanelForTableCheckConstraints(viewerTable));
        }
      }
    } else {
      Label noCheckConstraints = new Label("This schema does not have any check constraints.");
      noCheckConstraints.addStyleName("strong");
      contentItems.add(noCheckConstraints);
    }
  }

  private BasicTablePanel<ViewerCheckConstraint> getBasicTablePanelForTableCheckConstraints(final ViewerTable table) {
    Label header = new Label("Constraints in table " + table.getName());
    header.addStyleName("h4");

    HTMLPanel info = new HTMLPanel("");

    return new BasicTablePanel<>(header, info, table.getCheckConstraints().iterator(),

    new BasicTablePanel.ColumnInfo<>("Name", 15, new TextColumn<ViewerCheckConstraint>() {
      @Override
      public String getValue(ViewerCheckConstraint viewerRoutineParameter) {
        return viewerRoutineParameter.getName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Condition", 15, new TextColumn<ViewerCheckConstraint>() {
      @Override
      public String getValue(ViewerCheckConstraint viewerRoutineParameter) {
        if (ViewerStringUtils.isNotBlank(viewerRoutineParameter.getCondition())) {
          return viewerRoutineParameter.getCondition();
        } else {
          return "";
        }
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Description", 35, new TextColumn<ViewerCheckConstraint>() {
      @Override
      public String getValue(ViewerCheckConstraint viewerRoutineParameter) {
        if (ViewerStringUtils.isNotBlank(viewerRoutineParameter.getDescription())) {
          return viewerRoutineParameter.getDescription();
        } else {
          return "";
        }
      }
    })

    );
  }
}
