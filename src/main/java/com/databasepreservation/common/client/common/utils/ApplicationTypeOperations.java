package com.databasepreservation.common.client.common.utils;

import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.tools.RestUtils;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.Window;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class ApplicationTypeOperations {

  // TODO: need tests
  public static void showSIARDInFolderOrDownload(String path) {
    if (path == null || path.trim().isEmpty())
      return;

    if (ApplicationType.isDesktop()) {
      JavascriptUtils.showItemInFolder(path);
    } else if (ApplicationType.isDesktopForWeb()) {
      triggerDownload(RestUtils.createFileResourceDownloadSIARDUri(path));
    } else {
      triggerDownload(RestUtils.createFileResourceDownloadSIARDUri(path));
    }
  }

  // TODO: need tests
  public static void showValidationReportOrDownload(String reportPath, String databaseUuid) {
    if (ApplicationType.isDesktop()) {
      if (reportPath != null && !reportPath.isEmpty()) {
        JavascriptUtils.showItem(reportPath);
      }
    } else {
      triggerDownload(RestUtils.createFileResourceDownloadValidationReportUri(databaseUuid));
    }
  }

  private static void triggerDownload(SafeUri uri) {
    Window.Location.assign(uri.asString());
  }

  // TODO: need tests
  public static void requestConfirmation(String title, String message, String cancelBtn, String confirmBtn,
    DefaultAsyncCallback<Boolean> callback) {
    if (ApplicationType.isDesktop()) {
      JavascriptUtils.confirmationDialog(title, message, cancelBtn, confirmBtn, callback);
    } else {
      Dialogs.showConfirmDialog(title, message, cancelBtn, confirmBtn, callback);
    }
  }

  // TODO: need tests
  public static void choosePathToOpenAsync(JavaScriptObject desktopOptions, String webTitle, String webMessage,
    String webCancelBtn, String webOpenBtn, DefaultAsyncCallback<String> callback) {
    if (ApplicationType.isDesktop()) {
      String path = JavascriptUtils.openFileDialog(desktopOptions);
      if (path != null) {
        callback.onSuccess(path);
      }
    } else {
      Dialogs.showServerFilePathDialog(webTitle, webMessage, webCancelBtn, webOpenBtn, callback);
    }
  }

  // TODO: need tests
  public static void choosePathToSaveAsync(JavaScriptObject desktopOptions, String webTitle, String webMessage,
    String webCancelBtn, String webSaveBtn, DefaultAsyncCallback<String> callback) {
    if (ApplicationType.isDesktop()) {
      String path = JavascriptUtils.saveFileDialog(desktopOptions);
      if (path != null) {
        callback.onSuccess(path);
      }
    } else {
      Dialogs.showServerFilePathDialog(webTitle, webMessage, webCancelBtn, webSaveBtn, callback);
    }
  }
}
