package com.databasepreservation.main.desktop.client.dbptk.metadata.schemas.views;

import com.databasepreservation.main.common.server.index.factory.SolrClientFactory;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerDatabase;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSIARDBundle;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerSchema;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerTable;
import com.databasepreservation.main.common.shared.ViewerStructure.ViewerView;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.desktop.client.dbptk.metadata.MetadataPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import org.apache.solr.client.solrj.SolrClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class MetadataView extends MetadataPanel {

    @Override
    public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {

    }

    interface MetadataViewUiBinder extends UiBinder<Widget, MetadataView> {}

    private static MetadataViewUiBinder uiBinder = GWT.create(MetadataViewUiBinder.class);

    private static Map<String, MetadataView> instances = new HashMap<>();
    private ViewerSIARDBundle SIARDbundle;
    private ViewerDatabase database;
    private ViewerSchema schema;
    private ViewerView view;


    public static MetadataView getInstance(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String schemaUUID, String viewUUID) {
        String separator = "/";
        String code = database.getUUID() + separator + viewUUID;

        MetadataView instance = instances.get(code);
        if(instance == null) {
            instance = new MetadataView(database, SIARDbundle, schemaUUID, viewUUID);
            instances.put(code, instance);
        }

        return instance;
    }

    private MetadataView(ViewerDatabase database, ViewerSIARDBundle SIARDbundle, String schemaUUID, String viewUUID) {
        this.database = database;
        this.SIARDbundle = SIARDbundle;
        //view = database.getMetadata().getSchema(schemaUUID).getView(viewUUID);
    }
}