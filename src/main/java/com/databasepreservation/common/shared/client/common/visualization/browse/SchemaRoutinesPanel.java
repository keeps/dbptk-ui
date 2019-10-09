package com.databasepreservation.common.shared.client.common.visualization.browse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.shared.ViewerConstants;
import com.databasepreservation.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.common.shared.ViewerStructure.ViewerRoutine;
import com.databasepreservation.common.shared.ViewerStructure.ViewerRoutineParameter;
import com.databasepreservation.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.shared.client.common.RightPanel;
import com.databasepreservation.common.shared.client.common.lists.BasicTablePanel;
import com.databasepreservation.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.common.shared.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.common.shared.client.tools.ViewerStringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SchemaRoutinesPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, SchemaRoutinesPanel> instances = new HashMap<>();

  public static SchemaRoutinesPanel getInstance(ViewerDatabase database, String schemaUUID) {
    String separator = "/";
    String code = database.getUUID() + separator + schemaUUID;

    SchemaRoutinesPanel instance = instances.get(code);
    if (instance == null) {
      instance = new SchemaRoutinesPanel(database, schemaUUID);
      instances.put(code, instance);
    }
    return instance;
  }

  interface SchemaRoutinesUiBinder extends UiBinder<Widget, SchemaRoutinesPanel> {
  }

  private static SchemaRoutinesUiBinder uiBinder = GWT.create(SchemaRoutinesUiBinder.class);

  private ViewerDatabase database;
  private ViewerSchema schema;

  @UiField
  FlowPanel contentItems;

  private SchemaRoutinesPanel(ViewerDatabase database, final String schemaUUID) {
    this.database = database;
    schema = database.getMetadata().getSchema(schemaUUID);

    initWidget(uiBinder.createAndBindUi(this));

    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
      BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forDesktopSchemaRoutines(database.getMetadata().getName(),
          database.getUUID(), schema.getName(), schema.getUUID()));
    } else {
      BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forSchemaRoutines(database.getMetadata().getName(),
          database.getUUID(), schema.getName(), schema.getUUID()));
    }
  }

  private void init() {
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
      Label noRoutinesMsg = new Label(messages.routines_thisSchemaDoesNotHaveAnyRoutines());
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

    JavascriptUtils.runHighlighter(contentItems.getElement());
  }

  private HTMLPanel getRoutineDescription(ViewerRoutine viewerRoutine) {
    SafeHtmlBuilder descriptionBuilder = new SafeHtmlBuilder();

    if (ViewerStringUtils.isNotBlank(viewerRoutine.getName())) {
      descriptionBuilder.append(CommonClientUtils.getFieldHTML(messages.name(), viewerRoutine.getName()));
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getDescription())) {
      descriptionBuilder.append(CommonClientUtils.getFieldHTML(messages.description(), viewerRoutine.getDescription()));
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getSource())) {
      descriptionBuilder
        .append(CommonClientUtils.getFieldHTML(messages.routine_sourceCode(), viewerRoutine.getSource()));
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getBody())) {
      descriptionBuilder.append(CommonClientUtils.getFieldHTML(messages.routine_body(), SafeHtmlUtils
        .fromSafeConstant("<pre><code>" + SafeHtmlUtils.htmlEscape(viewerRoutine.getBody()) + "</code></pre>")));
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getCharacteristic())) {
      descriptionBuilder
        .append(CommonClientUtils.getFieldHTML(messages.routine_characteristic(), viewerRoutine.getCharacteristic()));
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getReturnType())) {
      descriptionBuilder
        .append(CommonClientUtils.getFieldHTML(messages.routine_returnType(), viewerRoutine.getReturnType()));
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

      new BasicTablePanel.ColumnInfo<>(messages.name(), 15, new TextColumn<ViewerRoutineParameter>() {
        @Override
        public String getValue(ViewerRoutineParameter viewerRoutineParameter) {
          return viewerRoutineParameter.getName();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.routineParameter_mode(), 15, new TextColumn<ViewerRoutineParameter>() {
        @Override
        public String getValue(ViewerRoutineParameter viewerRoutineParameter) {
          return viewerRoutineParameter.getMode();
        }
      }),

      new BasicTablePanel.ColumnInfo<>(messages.typeName(), 15, new TextColumn<ViewerRoutineParameter>() {
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

      new BasicTablePanel.ColumnInfo<>(messages.originalTypeName(), 15, new TextColumn<ViewerRoutineParameter>() {
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

      new BasicTablePanel.ColumnInfo<>(messages.description(), 35, new TextColumn<ViewerRoutineParameter>() {
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
