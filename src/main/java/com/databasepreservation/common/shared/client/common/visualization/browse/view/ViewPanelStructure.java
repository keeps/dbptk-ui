package com.databasepreservation.common.shared.client.common.visualization.browse.view;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.shared.ViewerStructure.ViewerColumn;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.common.shared.ViewerStructure.ViewerView;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.common.RightPanel;
import com.databasepreservation.common.shared.client.common.lists.BasicTablePanel;
import com.databasepreservation.common.shared.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.common.shared.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.shared.client.tools.HistoryManager;
import com.databasepreservation.common.shared.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ViewPanelStructure extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, ViewPanelStructure> instances = new HashMap<>();

  interface SchemaViewsPanelUiBinder extends UiBinder<Widget, ViewPanelStructure> {
  }

  private static SchemaViewsPanelUiBinder uiBinder = GWT.create(SchemaViewsPanelUiBinder.class);

  private ViewerDatabase database;
  private ViewerSchema schema;
  private ViewerView view;

  @UiField
  FlowPanel viewStructure;

  @UiField
  FlowPanel query;

  @UiField
  SimplePanel mainHeader;

  @UiField
  Button btnBack;

  @UiField
  Button options;

  public static ViewPanelStructure getInstance(ViewerDatabase database, String viewUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + viewUUID;

    ViewPanelStructure instance = instances.get(code);
    if (instance == null) {
      instance = new ViewPanelStructure(database, viewUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  private ViewPanelStructure(ViewerDatabase viewerDatabase, final String viewUUID) {
    database = viewerDatabase;
    schema = database.getMetadata().getSchemaFromViewUUID(viewUUID);
    view = database.getMetadata().getView(viewUUID);

    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
      BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forSchemaViews(database.getMetadata().getName(),
        database.getUUID(), schema.getName(), schema.getUUID()));
  }

  private void init() {
    mainHeader.setWidget(CommonClientUtils.getHeader(FontAwesomeIconManager.getTag(FontAwesomeIconManager.SCHEMA_VIEWS),
      view.getName(), "h1"));

    configureButtons();

    viewStructure.add(getBasicTablePanelForViewColumns(view));
    query.add(new HTML(CommonClientUtils.constructViewQuery(view).toSafeHtml()));

    JavascriptUtils.runHighlighter(query.getElement());
  }

  private void configureButtons() {
    btnBack.setText(messages.basicActionBack());

    btnBack.addClickHandler(event -> HistoryManager.gotoView(database.getUUID(), view.getUUID()));

    options.setText(messages.schemaStructurePanelTextForAdvancedOption());

    options.addClickHandler(event -> HistoryManager.gotoView(database.getUUID(), view.getUUID()));
  }

  private BasicTablePanel<ViewerColumn> getBasicTablePanelForViewColumns(ViewerView view) {
    // create and return the table panel
    Label header = new Label(messages.menusidebar_views());
    header.addStyleName("h5");
    header.setVisible(false);

    return new BasicTablePanel<ViewerColumn>(header, SafeHtmlUtils.EMPTY_SAFE_HTML, view.getColumns().iterator(),

      new BasicTablePanel.ColumnInfo<>(messages.columnName(), 15, new TextColumn<ViewerColumn>() {
        @Override
        public String getValue(ViewerColumn column) {
          return column.getDisplayName();
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
      }));
  }
}
