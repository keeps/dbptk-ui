package com.databasepreservation.main.visualization.client.browse;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.client.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.tools.BreadcrumbManager;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class NewUploadPanel extends RightPanel {
  interface NewUploadPanelUiBinder extends UiBinder<Widget, NewUploadPanel> {
  }

  private static NewUploadPanel instance = null;

  public static NewUploadPanel getInstance() {
    if (instance == null) {
      instance = new NewUploadPanel();
    }
    return instance;
  }

  private static NewUploadPanelUiBinder uiBinder = GWT.create(NewUploadPanelUiBinder.class);

  @UiField
  TextBox textBoxName;

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  public NewUploadPanel() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  /**
   * Uses BreadcrumbManager to show available information in the breadcrumbPanel
   *
   * @param breadcrumb
   *          the BreadcrumbPanel for this database
   */
  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forNewUpload());
  }

  @UiHandler("buttonApply")
  void handleButtonApply(ClickEvent e) {
    buttonApply.setEnabled(false);
    buttonCancel.setEnabled(false);

    // update info & commit
    BrowserService.Util.getInstance().uploadSIARD(textBoxName.getText(), new DefaultAsyncCallback<String>() {
      @Override
      public void onFailure(Throwable caught) {
        // error, don't go anywhere
        // buttonApply.setEnabled(true);
        // buttonCancel.setEnabled(true);
        // super.onFailure(caught);
      }

      @Override
      public void onSuccess(String newDatabaseUUID) {
        // buttonApply.setEnabled(true);
        // buttonCancel.setEnabled(true);
        //
        // GWT.log("new ID: " + newDatabaseUUID);
        //
        // HistoryManager.gotoDatabaseList();
      }
    });
    HistoryManager.gotoDatabaseList();
  }

  @UiHandler("buttonCancel")
  void handleButtonCancel(ClickEvent e) {
    HistoryManager.gotoRoot();
  }

  @Override
  protected void onDetach() {
    super.onDetach();
    buttonApply.setEnabled(true);
    buttonCancel.setEnabled(true);
    textBoxName.setText(null);
  }
}
