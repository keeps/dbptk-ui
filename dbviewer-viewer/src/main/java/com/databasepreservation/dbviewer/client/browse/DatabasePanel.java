package com.databasepreservation.dbviewer.client.browse;

import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerColumn;
import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerMetadata;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class DatabasePanel extends Composite {
  interface DatabasePanelUiBinder extends UiBinder<Widget, DatabasePanel> {
  }

  private static DatabasePanelUiBinder uiBinder = GWT.create(DatabasePanelUiBinder.class);

  private String databaseID;

  private ViewerDatabase database;
  private ViewerMetadata metadata;

  @UiField
  public Label idElem;

  public DatabasePanel(final String databaseID) {
    this.databaseID = databaseID;

    initWidget(uiBinder.createAndBindUi(this));

    BrowserService.Util.getInstance().retrieve(ViewerDatabase.class.getName(), databaseID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          idElem.setText(caught.getMessage());
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          metadata = database.getMetadata();

          // just output something interesting
          String tables = "\n";
          for (ViewerSchema viewerSchema : metadata.getSchemas()) {
            for (ViewerTable viewerTable : viewerSchema.getTables()) {
              tables += "[" + viewerSchema.getName() + "." + viewerTable.getName() + "]: \n";
              for (ViewerColumn viewerColumn : viewerTable.getColumns()) {
                tables += "   " + viewerColumn.toString() + "\n";
              }
            }
          }

          idElem.setText("DB name: " + metadata.getName() + "; DB UUID: " + database.getUUID() + "; tables list: "
            + tables);
        }
      });
  }
}
