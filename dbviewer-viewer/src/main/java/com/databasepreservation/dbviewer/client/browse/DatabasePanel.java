package com.databasepreservation.dbviewer.client.browse;

import org.roda.core.data.v2.index.IsIndexed;

import com.databasepreservation.dbviewer.client.BrowserService;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerDatabase;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerMetadata;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerSchema;
import com.databasepreservation.dbviewer.client.ViewerStructure.ViewerTable;
import com.databasepreservation.dbviewer.shared.client.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
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
  VerticalPanel vPanel;

  public static void DatabasePanel() {

  }

  public DatabasePanel(final String databaseID) {
    this.databaseID = databaseID;

    initWidget(uiBinder.createAndBindUi(this));
    vPanel.setSpacing(5);
    vPanel.setVisible(true);

    BrowserService.Util.getInstance().retrieve(ViewerDatabase.class.getName(), databaseID,
      new AsyncCallback<IsIndexed>() {
        @Override
        public void onFailure(Throwable caught) {
          vPanel.clear();
          HTML headingElement = new HTML();
          headingElement.setHTML("<span>" + caught.getMessage() + "</span>");
          vPanel.add(headingElement);
          throw new RuntimeException(caught);
        }

        @Override
        public void onSuccess(IsIndexed result) {
          database = (ViewerDatabase) result;
          metadata = database.getMetadata();
          vPanel.clear();

          HTML headingElement = new HTML();
          headingElement.setHTML("<h4>Database: " + database.getMetadata().getName() + "</h4>");
          vPanel.add(headingElement);

          for (ViewerSchema schema : metadata.getSchemas()) {
            for (ViewerTable table : schema.getTables()) {
              vPanel.add(getHyperlink(schema.getName() + "." + table.getName(), database.getUUID(), table.getUUID()));
            }
          }
        }
      });
  }

  private Hyperlink getHyperlink(String display_text, String database_uuid, String table_uuid) {
    Hyperlink link = new Hyperlink(display_text, HistoryManager.linkToTable(database_uuid, table_uuid));
    return link;
  }
}
