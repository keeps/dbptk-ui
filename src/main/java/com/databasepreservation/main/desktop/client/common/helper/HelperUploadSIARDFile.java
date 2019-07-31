package com.databasepreservation.main.desktop.client.common.helper;

import java.util.Collections;

import com.databasepreservation.main.common.client.BrowserService;
import com.databasepreservation.main.common.shared.ViewerConstants;
import com.databasepreservation.main.common.shared.client.common.DefaultAsyncCallback;
import com.databasepreservation.main.common.shared.client.common.dialogs.Dialogs;
import com.databasepreservation.main.common.shared.client.common.utils.ApplicationType;
import com.databasepreservation.main.common.shared.client.common.utils.JavascriptUtils;
import com.databasepreservation.main.common.shared.client.tools.HistoryManager;
import com.databasepreservation.main.common.shared.client.tools.JSOUtils;
import com.databasepreservation.main.desktop.shared.models.Filter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class HelperUploadSIARDFile {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private String path;
  private Widget loading = new HTML(SafeHtmlUtils.fromSafeConstant(
    "<div id='loading' class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

  public void openFile(FlowPanel panel) {
    path = getSIARDPath();

    if (path != null) {
      panel.add(loading);
      BrowserService.Util.getInstance().findSIARDFile(path, new DefaultAsyncCallback<String>() {
        @Override
        public void onSuccess(String databaseUUID) {
          if (databaseUUID != null) {
            if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {
              JavascriptUtils.confirmationDialog(messages.dialogReimportSIARDTitle(), messages.dialogReimportSIARD(),
                messages.dialogCancel(), messages.dialogConfirm(), new DefaultAsyncCallback<Boolean>() {

                  @Override
                  public void onSuccess(Boolean confirm) {
                    successHandler(confirm, panel, databaseUUID);
                  }

                });
            } else {
              Dialogs.showConfirmDialog(messages.dialogReimportSIARDTitle(), messages.dialogReimportSIARD(),
                messages.dialogCancel(), messages.dialogConfirm(), new DefaultAsyncCallback<Boolean>() {
                  @Override
                  public void onSuccess(Boolean confirm) {
                    successHandler(confirm, panel, databaseUUID);
                  }
                });
            }
          } else {
            uploadMetadataSIARD(path, panel);
          }
        }
      });
    }
  }

  private String getSIARDPath() {
    String path;
    if (ApplicationType.getType().equals(ViewerConstants.ELECTRON)) {

      Filter siard = new Filter("SIARD", Collections.singletonList("siard"));

      JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.singletonList("openFile"),
        Collections.singletonList(siard));

      path = JavascriptUtils.openFileDialog(options);
    } else {
      path = "/home/gbarros/Desktop/test.siard";
      // TODO: UPLOAD FILE!!!
    }

    return path;
  }

  private void uploadMetadataSIARD(String path, FlowPanel panel) {
    BrowserService.Util.getInstance().generateUUID(new DefaultAsyncCallback<String>() {
      @Override
      public void onSuccess(String databaseUUID) {
        BrowserService.Util.getInstance().uploadMetadataSIARD(databaseUUID, path, new DefaultAsyncCallback<String>() {
          @Override
          public void onFailure(Throwable caught) {
            // TODO: error handling
            panel.remove(loading);
          }

          @Override
          public void onSuccess(String newDatabaseUUID) {
            panel.remove(loading);
            HistoryManager.gotoSIARDInfo(newDatabaseUUID);
          }
        });
      }
    });
  }

  private void successHandler(Boolean confirm, FlowPanel panel, String databaseUUID) {
    if (confirm) {
      uploadMetadataSIARD(path, panel);
    } else {
      panel.remove(loading);
      HistoryManager.gotoSIARDInfo(databaseUUID);
    }
  }
}
