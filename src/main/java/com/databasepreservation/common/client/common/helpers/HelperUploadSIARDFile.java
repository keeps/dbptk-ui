package com.databasepreservation.common.client.common.helpers;

import java.util.Collections;

import com.databasepreservation.common.client.BrowserService;
import com.databasepreservation.common.client.models.ExtensionFilter;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.JSOUtils;
import com.databasepreservation.common.client.widgets.Toast;
import com.databasepreservation.common.client.services.SIARDService;
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
  private Widget loading = new HTML(SafeHtmlUtils.fromSafeConstant(
    "<div id='loading' class='spinner'><div class='double-bounce1'></div><div class='double-bounce2'></div></div>"));

  public void openFile(FlowPanel panel) {
    if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {

      ExtensionFilter siard = new ExtensionFilter("SIARD", Collections.singletonList("siard"));

      JavaScriptObject options = JSOUtils.getOpenDialogOptions(Collections.singletonList("openFile"),
        Collections.singletonList(siard));

      openSIARDPath(panel, JavascriptUtils.openFileDialog(options));
    } else {
      Dialogs.showServerFilePathDialog(messages.managePageButtonTextForOpenSIARD(), messages.dialogOpenSIARDMessage(), messages.basicActionCancel(),
        messages.basicActionOpen(), new DefaultAsyncCallback<String>() {
          @Override
          public void onSuccess(String path) {
            openSIARDPath(panel, path);
          }
        });
    }
  }

  private void openSIARDPath(FlowPanel panel, String path) {
    if (path != null) {
      panel.add(loading);
      SIARDService.Util.call((String databaseUUID) -> {
        if (databaseUUID != null) {
          if (ApplicationType.getType().equals(ViewerConstants.DESKTOP)) {
            JavascriptUtils.confirmationDialog(messages.dialogReimportSIARDTitle(), messages.dialogReimportSIARD(),
              messages.basicActionCancel(), messages.basicActionConfirm(), new DefaultAsyncCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean confirm) {
                  successHandler(confirm, panel, databaseUUID, path);
                }
              });
          } else {
            Dialogs.showConfirmDialog(messages.dialogReimportSIARDTitle(), messages.dialogReimportSIARD(),
              messages.basicActionCancel(), messages.basicActionConfirm(), new DefaultAsyncCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean confirm) {
                  successHandler(confirm, panel, databaseUUID, path);
                }
              });
          }
        } else {
          uploadMetadataSIARD(path, panel);
        }

      }).findSIARDFile(path);
    }
  }

  private void uploadMetadataSIARD(String path, FlowPanel panel) {
    DatabaseService.Util.call((String result)->{
      SIARDService.Util.call((String newDatabaseUUID) -> {
        panel.remove(loading);
        HistoryManager.gotoSIARDInfo(newDatabaseUUID);
      }, (String errorMessage) -> {
        Toast.showError(messages.errorMessagesOpenFile(), errorMessage);
        panel.remove(loading);
      }).uploadMetadataSIARD(result, path);
    }).generateUUID();
  }

  private void successHandler(Boolean confirm, FlowPanel panel, String databaseUUID, String path) {
    if (confirm) {
      uploadMetadataSIARD(path, panel);
    } else {
      panel.remove(loading);
      HistoryManager.gotoSIARDInfo(databaseUUID);
    }
  }
}
