package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.routines;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.databasepreservation.main.common.shared.ViewerStructure.*;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.desktop.client.common.sidebar.MetadataEditSidebar;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

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

    public static MetadataPanel getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String schemaUUID, String routineUUID) {
        String separator = "/";
        String code = database.getUUID() + separator + routineUUID;

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

        GWT.log("MetadataRoutinePanel::" + routine.getName());

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
        description.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                routine.setDescription(description.getText());
                SIARDbundle.setRoutine(schema.getName(), routine.getName(), description.getText());
                JavascriptUtils.alertUpdatedMetadata();
            }
        });

        tabPanel.add(new MetadataRoutineInfos(routine).createInfo(), messages.menusidebar_information());
        tabPanel.add(new MetadataRoutineParameters(SIARDbundle, schema, routine.getParameters()).createTable(), messages.routines_parametersList());

        tabPanel.selectTab(0);
    }
}
