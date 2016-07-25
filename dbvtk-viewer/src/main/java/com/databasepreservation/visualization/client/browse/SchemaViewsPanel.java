package com.databasepreservation.visualization.client.browse;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.visualization.client.ViewerStructure.ViewerColumn;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.visualization.client.ViewerStructure.ViewerView;
import com.databasepreservation.visualization.client.common.lists.BasicTablePanel;
import com.databasepreservation.visualization.client.common.utils.CommonClientUtils;
import com.databasepreservation.visualization.client.common.utils.JavascriptUtils;
import com.databasepreservation.visualization.client.main.BreadcrumbPanel;
import com.databasepreservation.visualization.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.visualization.shared.client.Tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SchemaViewsPanel extends RightPanel {
  private static Map<String, SchemaViewsPanel> instances = new HashMap<>();

  public static SchemaViewsPanel getInstance(ViewerDatabase database, String schemaUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + schemaUUID;

    SchemaViewsPanel instance = instances.get(code);
    if (instance == null) {
      instance = new SchemaViewsPanel(database, schemaUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, SchemaViewsPanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerSchema schema;

  @UiField
  FlowPanel contentItems;

  private SchemaViewsPanel(ViewerDatabase viewerDatabase, final String schemaUUID) {
    database = viewerDatabase;
    schema = database.getMetadata().getSchema(schemaUUID);

    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(
      breadcrumb,
      BreadcrumbManager.forSchemaViews(database.getMetadata().getName(), database.getUUID(), schema.getName(),
        schema.getUUID()));
  }

  private void init() {
    CommonClientUtils.addSchemaInfoToFlowPanel(contentItems, schema);

    for (ViewerView viewerView : schema.getViews()) {
      contentItems.add(getBasicTablePanelForViewColumns(viewerView));
    }

    JavascriptUtils.runHighlighter(contentItems.getElement());
  }

  private BasicTablePanel<ViewerColumn> getBasicTablePanelForViewColumns(ViewerView view) {
    FlowPanel header = CommonClientUtils.getSchemaAndViewHeader(database.getUUID(), schema, view, "h3");

    SafeHtmlBuilder infoBuilder = new SafeHtmlBuilder();
    infoBuilder.append(CommonClientUtils.getFieldHTML("Description", view.getDescription()));

    if (ViewerStringUtils.isNotBlank(view.getQueryOriginal())) {
      infoBuilder.append(CommonClientUtils.getFieldHTML(
        "Original query",
        SafeHtmlUtils.fromSafeConstant("<pre><code>" + SafeHtmlUtils.htmlEscape(view.getQueryOriginal())
          + "</code></pre>")));
    }

    if (ViewerStringUtils.isNotBlank(view.getQuery())) {
      infoBuilder.append(CommonClientUtils.getFieldHTML("Query",
        SafeHtmlUtils.fromSafeConstant("<pre><code>" + SafeHtmlUtils.htmlEscape(view.getQuery()) + "</code></pre>")));
    }

    // create and return the table panel
    return new BasicTablePanel<>(header, infoBuilder.toSafeHtml(), view.getColumns().iterator(),

    new BasicTablePanel.ColumnInfo<>("column name", 15, new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        return column.getDisplayName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Type name", 15, new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        return column.getType().getTypeName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Original type name", 15, new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        return column.getType().getOriginalTypeName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Nullable", 8, new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        if (column.getNillable()) {
          return "Yes";
        } else {
          return "No";
        }
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Description", 35, new TextColumn<ViewerColumn>() {
      @Override
      public String getValue(ViewerColumn column) {
        if (ViewerStringUtils.isNotBlank(column.getDescription())) {
          return column.getDescription();
        } else {
          return "";
        }
      }
    })

    );
  }
}
