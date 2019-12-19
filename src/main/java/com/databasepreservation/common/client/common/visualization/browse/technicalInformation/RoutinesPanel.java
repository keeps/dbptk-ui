package com.databasepreservation.common.client.common.visualization.browse.technicalInformation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.GenericField;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.lists.widgets.BasicTablePanel;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRoutine;
import com.databasepreservation.common.client.models.structure.ViewerRoutineParameter;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.ViewerStringUtils;
import com.databasepreservation.common.client.widgets.Alert;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class RoutinesPanel extends RightPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static Map<String, RoutinesPanel> instances = new HashMap<>();

  public static RoutinesPanel getInstance(ViewerDatabase database) {
    return instances.computeIfAbsent(database.getUuid(), k -> new RoutinesPanel(database));
  }

  interface SchemaRoutinesUiBinder extends UiBinder<Widget, RoutinesPanel> {
  }

  private static SchemaRoutinesUiBinder uiBinder = GWT.create(SchemaRoutinesUiBinder.class);

  private ViewerDatabase database;

  @UiField
  FlowPanel contentItems;

  @UiField
  Label title;

  private RoutinesPanel(ViewerDatabase database) {
    this.database = database;
    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forSchemaRoutines(database.getUuid(), database.getMetadata().getName()));
  }

  private void init() {
    if (database.getMetadata().getSchemas().size() == 1) {
      contentItems.add(buildRoutinesForSingleSchema(database.getMetadata().getSchemas().get(0)));
    } else {
      buildRoutinesForMultipleSchemas();
    }

    title.setText(messages.menusidebar_routines());

    JavascriptUtils.runHighlighter(contentItems.getElement());
  }

  private FlowPanel buildRoutinesForSingleSchema(ViewerSchema schema) {
    FlowPanel panel = new FlowPanel();

    List<ViewerRoutine> routines = new ArrayList<>(schema.getRoutines());
    routines.sort(Comparator.comparing(ViewerRoutine::getName));

    if (routines.isEmpty()) {
      panel.add(new Alert(Alert.MessageAlertType.WARNING, messages.routines_thisSchemaDoesNotHaveAnyRoutines()));
    } else {
      for (ViewerRoutine viewerRoutine : routines) {
        if (viewerRoutine.getParameters().isEmpty()) {
          panel.add(addRoutineHeaderAndDescription(viewerRoutine));
        } else {
          panel.add(getBasicTablePanelForSchemaRoutines(viewerRoutine));
        }
      }
    }

    return panel;
  }

  private void buildRoutinesForMultipleSchemas() {
    TabPanel tabPanel = new TabPanel();
    for (ViewerSchema schema : database.getMetadata().getSchemas()) {
      tabPanel.addStyleName("browseItemMetadata");
      tabPanel.add(buildRoutinesForSingleSchema(schema), schema.getName());

    }
    tabPanel.selectTab(0);
    contentItems.add(tabPanel);
  }

  private FlowPanel getRoutineDescription(ViewerRoutine viewerRoutine) {
    FlowPanel panel = new FlowPanel();

    if (ViewerStringUtils.isNotBlank(viewerRoutine.getName())) {
      MetadataField schemaName = MetadataField.createInstance(messages.name(), viewerRoutine.getName());
      schemaName.setCSS("metadata-field", "metadata-information-element-label",
              "metadata-information-element-value");
      panel.add(schemaName);
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getDescription())) {
      MetadataField description = MetadataField.createInstance(messages.description(), viewerRoutine.getDescription());
      description.setCSS("metadata-field", "metadata-information-element-label",
              "metadata-information-element-value");
      panel.add(description);
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getSource())) {
      MetadataField sourceCode = MetadataField.createInstance(messages.routine_sourceCode(), viewerRoutine.getSource());
      sourceCode.setCSS("metadata-field", "metadata-information-element-label",
              "metadata-information-element-value");
      panel.add(sourceCode);
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getBody())) {
      GenericField field = GenericField.createInstance(messages.routine_body(), new HTMLPanel(SafeHtmlUtils
              .fromSafeConstant("<pre><code>" + SafeHtmlUtils.htmlEscape(viewerRoutine.getBody()) + "</code></pre>")));
      field.setCSSMetadata("metadata-field", "metadata-information-element-label");
      panel.add(field);
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getCharacteristic())) {
      MetadataField field = MetadataField.createInstance(messages.routine_characteristic(), viewerRoutine.getCharacteristic());
      field.setCSS("metadata-field", "metadata-information-element-label",
              "metadata-information-element-value");
      panel.add(field);
    }
    if (ViewerStringUtils.isNotBlank(viewerRoutine.getReturnType())) {
      MetadataField field = MetadataField.createInstance(messages.routine_returnType(), viewerRoutine.getReturnType());
      field.setCSS("metadata-field", "metadata-information-element-label",
              "metadata-information-element-value");
      panel.add(field);
    }

    return panel;
  }

  private FlowPanel addRoutineHeaderAndDescription(ViewerRoutine routine) {
    FlowPanel panel = new FlowPanel();
    panel.addStyleName("card");

    Label header = new Label(routine.getName());
    header.addStyleName("card-header");

    FlowPanel info = getRoutineDescription(routine);

    panel.add(header);
    panel.add(info);

    return panel;
  }

  private BasicTablePanel<ViewerRoutineParameter> getBasicTablePanelForSchemaRoutines(final ViewerRoutine routine) {
    Label header = new Label(routine.getName());
    header.addStyleName("h4");

    FlowPanel info = getRoutineDescription(routine);

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
