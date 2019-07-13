package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.routines;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerStructure.*;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import config.i18n.client.ClientMessages;

public class MetadataRoutine extends MetadataPanel {

    interface MetadataRoutineUiBinder extends UiBinder<Widget, MetadataRoutine> {
    }

    private static MetadataRoutineUiBinder uiBinder = GWT.create(MetadataRoutineUiBinder.class);

    @UiField
    SimplePanel mainHeader;

    @UiField
    TextArea description;

    @UiField
    FlowPanel source, body, characteristic, returnType, parameters;

    private static final ClientMessages messages = GWT.create(ClientMessages.class);
    private static Map<String, MetadataRoutine> instances = new HashMap<>();
    private ViewerSIARDBundle SIARDbundle;
    private ViewerDatabase database;
    private ViewerSchema schema;
    private ViewerRoutine routine;

    public static MetadataPanel getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String schemaUUID, String routineUUID) {
        String separator = "/";
        String code = database.getUUID() + separator + routineUUID;

        MetadataRoutine instance = instances.get(code);
        if (instance == null) {
            instance = new MetadataRoutine(database, schemaUUID, routineUUID, SIARDbundle);
            instances.put(code, instance);
        }

        return instance;
    }

    private MetadataRoutine(ViewerDatabase database, String schemaUUID, String routineUUID, ViewerSIARDBundle SIARDbundle) {
        this.database = database;
        this.SIARDbundle = SIARDbundle;
        routine = database.getMetadata().getRoutine(routineUUID);
        schema = database.getMetadata().getSchema(schemaUUID);

        GWT.log("MetadataRoutine::" + routine.getName());

        initWidget(uiBinder.createAndBindUi(this));
        init();
    }

    @Override
    public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {

    }

    private void init() {
        Label viewName = new Label();
        viewName.setText(routine.getName());
        mainHeader.setWidget(viewName);

        description
                .setText(routine.getDescription() == null ? messages.viewDoesNotContainDescription() : routine.getDescription());
        description.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                routine.setDescription(description.getText());
                SIARDbundle.setRoutine(schema.getName(), routine.getName(), description.getText());
            }
        });

        addContent(messages.routine_sourceCode(),routine.getSource(), source );
        addContent(messages.routine_body(),routine.getBody(), body );
        addContent(messages.routine_characteristic(),routine.getCharacteristic(), characteristic );
        addContent(messages.routine_returnType(),routine.getReturnType(), returnType );
        addContent(messages.routines_parametersList(),routine.getParameters(), returnType );
    }

    private void addContent(String headerLabel, String bodyValue, FlowPanel panel){
        Label label = new Label();
        Label value = new Label();

        label.setText(headerLabel);
        label.addStyleName("label");

        if(bodyValue != null && !bodyValue.isEmpty() ){
            value.setText(bodyValue);
        } else {
            value.setText(messages.routines_thisRoutineFieldDoesNotHaveContent());
        }
        value.addStyleName("value");

        panel.add(label);
        panel.add(value);
    }

    private void addContent(String headerLabel, List<ViewerRoutineParameter> bodylist, FlowPanel panel) {
        Label label = new Label();
        label.setText(headerLabel);
        label.addStyleName("label");
        panel.add(label);

        if(bodylist.isEmpty() ){
            Label value = new Label();
            value.setText(messages.routines_thisRoutineFieldDoesNotHaveContent());
            panel.add(value);
            value.addStyleName("value");
        } else {
            Label value = new Label();
            for (ViewerRoutineParameter param : bodylist) {
                value.setText(param.getName());
                value.addStyleName("h6");
                panel.add(value);

                value.setText(param.getDescription());
                value.addStyleName("value");
                panel.add(value);
            }
        }
    }
}
