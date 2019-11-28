package com.databasepreservation.common.client.common.visualization.metadata.schemas.routines;

import java.util.HashMap;
import java.util.Map;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.models.structure.ViewerRoutine;
import com.databasepreservation.common.client.models.structure.ViewerSIARDBundle;
import com.databasepreservation.common.client.models.structure.ViewerSchema;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataControlPanel;
import com.databasepreservation.common.client.common.visualization.metadata.MetadataPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class MetadataRoutinePanel extends MetadataPanel {

    interface MetadataRoutineUiBinder extends UiBinder<Widget, MetadataRoutinePanel> {
    }

    private static MetadataRoutineUiBinder uiBinder = GWT.create(MetadataRoutineUiBinder.class);

    @UiField
    SimplePanel mainHeader;

    @UiField
    TextArea description;

    @UiField
    TabPanel tabPanel;

    private static final ClientMessages messages = GWT.create(ClientMessages.class);
    private static Map<String, MetadataRoutinePanel> instances = new HashMap<>();
    private ViewerSIARDBundle SIARDbundle;
    private ViewerDatabase database;
    private ViewerSchema schema;
    private ViewerRoutine routine;
    private MetadataControlPanel controls;

    public static MetadataPanel getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String schemaUUID, String routineUUID) {
        String separator = "/";
        String code = database.getUuid() + separator + routineUUID;

        MetadataRoutinePanel instance = instances.get(code);
        if (instance == null) {
            instance = new MetadataRoutinePanel(database, schemaUUID, routineUUID, SIARDbundle);
            instances.put(code, instance);
        }
        return instance;
    }

    private MetadataRoutinePanel(ViewerDatabase database, String schemaUUID, String routineUUID, ViewerSIARDBundle SIARDbundle) {
        this.database = database;
        this.SIARDbundle = SIARDbundle;
        routine = database.getMetadata().getRoutine(routineUUID);
        schema = database.getMetadata().getSchema(schemaUUID);
        this.controls = MetadataControlPanel.getInstance(database.getUuid());

        initWidget(uiBinder.createAndBindUi(this));
        init();
    }

    @Override
    public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {

    }

    private void init() {
        Label viewName = new Label();
        viewName.setText(schema.getName()+"."+routine.getName());
        mainHeader.setWidget(viewName);

        description.getElement().setAttribute("placeholder", messages.routineDoesNotContainDescription());
        description.setText(routine.getDescription());
        description.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                routine.setDescription(description.getText());
                SIARDbundle.setRoutine(schema.getName(), routine.getName(), description.getText());
                controls.validate();
            }
        });
        description.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                description.selectAll();
            }
        });

        tabPanel.add(new MetadataRoutineInfos(routine).createInfo(), messages.menusidebar_information());
        tabPanel.add(new MetadataRoutineParameters(SIARDbundle, schema, routine.getParameters(), controls).createTable(), messages.routines_parametersList());

        tabPanel.selectTab(0);
    }
}
