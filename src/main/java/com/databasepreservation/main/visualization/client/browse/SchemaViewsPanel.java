package com.databasepreservation.main.visualization.client.browse;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerView;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.RightPanel;
import com.databasepreservation.main.common.shared.client.common.lists.BasicTablePanel;
import com.databasepreservation.main.common.shared.client.common.utils.CommonClientUtils;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SchemaViewsPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
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

  interface SchemaViewsPanelUiBinder extends UiBinder<Widget, SchemaViewsPanel> {
  }

  private static SchemaViewsPanelUiBinder uiBinder = GWT.create(SchemaViewsPanelUiBinder.class);

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
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forSchemaViews(database.getMetadata().getName(),
      database.getUUID(), schema.getName(), schema.getUUID()));
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
    infoBuilder.append(CommonClientUtils.getFieldHTML(messages.description(), view.getDescription()));

    if (ViewerStringUtils.isNotBlank(view.getQueryOriginal())) {
      infoBuilder.append(CommonClientUtils.getFieldHTML(messages.originalQuery(), SafeHtmlUtils
        .fromSafeConstant("<pre><code>" + SafeHtmlUtils.htmlEscape(view.getQueryOriginal()) + "</code></pre>")));
    }

    if (ViewerStringUtils.isNotBlank(view.getQuery())) {
      infoBuilder.append(CommonClientUtils.getFieldHTML(messages.query(),
        SafeHtmlUtils.fromSafeConstant("<pre><code>" + SafeHtmlUtils.htmlEscape(view.getQuery()) + "</code></pre>")));
    }

    // create and return the table panel
    return new BasicTablePanel<>(header, infoBuilder.toSafeHtml(), view.getColumns().iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.columnName(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn column) {
          return column.getDisplayName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.typeName(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn column) {
          return column.getType().getTypeName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.originalTypeName(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn column) {
          return column.getType().getOriginalTypeName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.nullable(), 8, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn column) {
          if (column.getNillable()) {
            return "Yes";
          } else {
            return "No";
          }
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.description(), 35, new TextColumn<ViewerColumn>() {
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
