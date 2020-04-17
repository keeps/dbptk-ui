package com.databasepreservation.common.client.common.helpers;

import java.util.Collections;

import org.roda.core.data.v2.index.sublist.Sublist;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.utils.ApplicationType;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.index.FindRequest;
import com.databasepreservation.common.client.index.IndexResult;
import com.databasepreservation.common.client.index.facets.Facets;
import com.databasepreservation.common.client.index.filter.Filter;
import com.databasepreservation.common.client.index.filter.SimpleFilterParameter;
import com.databasepreservation.common.client.index.sort.Sorter;
import com.databasepreservation.common.client.models.JSO.ExtensionFilter;
import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.JSOUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.LocaleInfo;
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
    if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {

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
      FindRequest request = new FindRequest(ViewerDatabase.class.getName(),
        new Filter(new SimpleFilterParameter(ViewerConstants.SOLR_DATABASES_SIARD_PATH, path)), Sorter.NONE,
        new Sublist(), Facets.NONE, false, Collections.singletonList(ViewerConstants.INDEX_ID));
      DatabaseService.Util.call((IndexResult<ViewerDatabase> result) -> {
        if (result.getTotalCount() == 1) {
          if (ApplicationType.getType().equals(ViewerConstants.APPLICATION_ENV_DESKTOP)) {
            JavascriptUtils.confirmationDialog(messages.dialogReimportSIARDTitle(), messages.dialogReimportSIARD(),
              messages.basicActionCancel(), messages.basicActionConfirm(), new DefaultAsyncCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean confirm) {
                  successHandler(confirm, panel, result.getResults().get(0).getUuid(), path);
                }
              });
          } else {
            Dialogs.showConfirmDialog(messages.dialogReimportSIARDTitle(), messages.dialogReimportSIARD(),
              messages.basicActionCancel(), messages.basicActionConfirm(), new DefaultAsyncCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean confirm) {
                  successHandler(confirm, panel, result.getResults().get(0).getUuid(), path);
                }
              });
          }
        } else if (result.getTotalCount() == 0) {
          uploadMetadataSIARD(path, panel);
        } else {

        }
      }).find(request, LocaleInfo.getCurrentLocale().getLocaleName());
    }
  }

  private void uploadMetadataSIARD(String path, FlowPanel panel) {
    DatabaseService.Util.call((String databaseUUID) -> {
        panel.remove(loading);
        HistoryManager.gotoSIARDInfo(databaseUUID);
      }, (String errorMessage) -> {
      Dialogs.showErrors(messages.errorMessagesOpenFile(path), errorMessage, messages.basicActionClose());
        //Toast.showError(messages.errorMessagesOpenFile(), errorMessage);
        panel.remove(loading);
    }).create(path);
  }

  private void successHandler(Boolean confirm, FlowPanel panel, String databaseUUID, String path) {
    if (confirm) {
      DatabaseService.Util.call((Boolean value) -> {
        uploadMetadataSIARD(path, panel);
      }).delete(databaseUUID);
    } else {
      panel.remove(loading);
      HistoryManager.gotoSIARDInfo(databaseUUID);
    }
  }
}
