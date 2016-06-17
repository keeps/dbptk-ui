package com.databasepreservation.dbviewer.client.browse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.adapter.filter.Filter;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerRoutine;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerRoutineParameter;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.common.lists.BasicTablePanel;
import com.databasepreservation.dbviewer.client.common.search.SearchPanel;
import com.databasepreservation.dbviewer.client.common.sidebar.DatabaseSidebar;
import com.databasepreservation.dbviewer.client.common.utils.CommonClientUtils;
import com.databasepreservation.dbviewer.client.main.BreadcrumbPanel;
import com.databasepreservation.dbviewer.shared.client.Tools.BreadcrumbManager;
import com.databasepreservation.dbviewer.shared.client.Tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
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
public class SchemaRoutinesPanel extends Composite {
  private static Map<String, SchemaRoutinesPanel> instances = new HashMap<>();

  public static SchemaRoutinesPanel getInstance(String databaseUUID, String schemaUUID) {
    String separator = "/";
    String code = databaseUUID + separator + schemaUUID;

    SchemaRoutinesPanel instance = instances.get(code);
    if (instance == null) {
      instance = new SchemaRoutinesPanel(databaseUUID, schemaUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface DatabasePanelUiBinder extends UiBinder<Widget, SchemaRoutinesPanel> {
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

  private SchemaRoutinesPanel(final String databaseUUID, final String schemaUUID) {
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

    // Routines and their information
    // Label tablesHeader = new Label("Routines");
    // tablesHeader.addStyleName("h2");
    // contentItems.add(tablesHeader);

    List<ViewerRoutine> routines = new ArrayList<ViewerRoutine>(schema.getRoutines());
    Collections.sort(routines, new Comparator<ViewerRoutine>() {
      @Override
      public int compare(ViewerRoutine o1, ViewerRoutine o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });

    if (routines.isEmpty()) {
      Label noRoutinesMsg = new Label("This schema does not have any routines.");
      noRoutinesMsg.addStyleName("strong");
      contentItems.add(noRoutinesMsg);
    } else {
      for (ViewerRoutine viewerRoutine : routines) {
        if (viewerRoutine.getParameters().isEmpty()) {
          addRoutineHeaderAndDescription(viewerRoutine);
        } else {
          contentItems.add(getBasicTablePanelForSchemaRoutines(viewerRoutine));
        }
      }
    }
  }

  private HTMLPanel getRoutineDescription(ViewerRoutine viewerRoutine) {
    SafeHtmlBuilder descriptionBuilder = new SafeHtmlBuilder();

    if (ViewerStringUtils.isNotBlank(viewerRoutine.getName())) {
      descriptionBuilder.append(CommonClientUtils.getFieldHTML("Name", viewerRoutine.getName()));
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getDescription())) {
      descriptionBuilder.append(CommonClientUtils.getFieldHTML("Description", viewerRoutine.getDescription()));
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getSource())) {
      descriptionBuilder.append(CommonClientUtils.getFieldHTML("Source", viewerRoutine.getSource()));
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getBody())) {
      descriptionBuilder.append(CommonClientUtils.getFieldHTML("Body", viewerRoutine.getBody()));
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getCharacteristic())) {
      descriptionBuilder.append(CommonClientUtils.getFieldHTML("Characteristic", viewerRoutine.getCharacteristic()));
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getReturnType())) {
      descriptionBuilder.append(CommonClientUtils.getFieldHTML("Return type", viewerRoutine.getReturnType()));
    }

    return new HTMLPanel(descriptionBuilder.toSafeHtml());
  }

  private void addRoutineHeaderAndDescription(ViewerRoutine routine) {
    Label header = new Label(routine.getName());
    header.addStyleName("h4");

    HTMLPanel info = getRoutineDescription(routine);

    contentItems.add(header);
    contentItems.add(info);
  }

  private BasicTablePanel<ViewerRoutineParameter> getBasicTablePanelForSchemaRoutines(final ViewerRoutine routine) {
    Label header = new Label(routine.getName());
    header.addStyleName("h4");

    HTMLPanel info = getRoutineDescription(routine);

    return new BasicTablePanel<ViewerRoutineParameter>(header, info, routine.getParameters().iterator(),

    new BasicTablePanel.ColumnInfo<>("Name", 15, new TextColumn<ViewerRoutineParameter>() {
      @Override
      public String getValue(ViewerRoutineParameter viewerRoutineParameter) {
        return viewerRoutineParameter.getName();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Mode", 15, new TextColumn<ViewerRoutineParameter>() {
      @Override
      public String getValue(ViewerRoutineParameter viewerRoutineParameter) {
        return viewerRoutineParameter.getMode();
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Type name", 15, new TextColumn<ViewerRoutineParameter>() {
      @Override
      public String getValue(ViewerRoutineParameter viewerRoutineParameter) {
        if (viewerRoutineParameter.getType() != null) {
          if (ViewerStringUtils.isNotBlank(viewerRoutineParameter.getType().getTypeName())) {
            return viewerRoutineParameter.getType().getTypeName();
          }
        }
        return "";
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Original type name", 15, new TextColumn<ViewerRoutineParameter>() {
      @Override
      public String getValue(ViewerRoutineParameter viewerRoutineParameter) {
        if (viewerRoutineParameter.getType() != null) {
          if (ViewerStringUtils.isNotBlank(viewerRoutineParameter.getType().getOriginalTypeName())) {
            return viewerRoutineParameter.getType().getOriginalTypeName();
          }
        }
        return "";
      }
    }),

    new BasicTablePanel.ColumnInfo<>("Description", 35, new TextColumn<ViewerRoutineParameter>() {
      @Override
      public String getValue(ViewerRoutineParameter viewerRoutineParameter) {
        if (viewerRoutineParameter.getType() != null) {
          if (ViewerStringUtils.isNotBlank(viewerRoutineParameter.getDescription())) {
            return viewerRoutineParameter.getDescription();
          }
        }
        return "";
      }
    })

    );
  }
}
