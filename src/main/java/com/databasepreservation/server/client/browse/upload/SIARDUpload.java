/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.server.client.browse.upload;

import java.util.ArrayList;
import java.util.List;

import org.roda.core.data.common.RodaConstants;

import com.databasepreservation.common.api.v1.utils.StringResponse;
import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.RightPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.dialogs.Dialogs;
import com.databasepreservation.common.client.common.utils.JavascriptUtils;
import com.databasepreservation.common.client.services.DatabaseService;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.HistoryManager;
import com.databasepreservation.common.client.tools.PathUtils;
import com.databasepreservation.common.client.widgets.Toast;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class SIARDUpload extends RightPanel {
  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forUpload());
  }

  interface SIARDUploadUiBinder extends UiBinder<Widget, SIARDUpload> {
  }

  private static SIARDUploadUiBinder uiBinder = GWT.create(SIARDUploadUiBinder.class);
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final String DRAGOVER = "dragover";
  private static List<Element> itemList;

  @UiField
  HTML uploadMessage;

  @UiField
  HTML uploadForm;

  @UiField
  HTML uploadList;

  private static SIARDUpload instance = null;

  private SIARDUpload() {
    initWidget(uiBinder.createAndBindUi(this));
    uploadList.setHTML(SafeHtmlUtils.fromSafeConstant("<ul id='upload-list'></ul>"));
  }

  /**
   * Get the singleton instance
   *
   * @return the instance
   */
  public static SIARDUpload getInstance() {
    if (instance == null) {
      instance = new SIARDUpload();
      itemList = new ArrayList<>();
    }
    return instance;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    itemList.clear();
  }

  private String getUploadUrl() {

    StringBuilder urlBuilder = new StringBuilder();
    String servlet = ViewerConstants.API_SERVLET;
    String resource = ViewerConstants.API_V1_FILE_RESOURCE;

    return urlBuilder.append(servlet).append(resource).toString();
  }

  @Override
  protected void onAttach() {
    super.onAttach();
    init();
  }

  protected void init() {

    uploadMessage.getElement().setId("upload-message");
    uploadMessage.setHTML("<span class='success'>" + messages.uploadSIARDTextForDoneUpload() + "</span>");
    uploadMessage.setVisible(false);

    updateUploadForm();
  }

  private String getLayout() {
    SafeHtmlBuilder b = new SafeHtmlBuilder();

    String item = "<li class='working'>"
      + "<input type='text' value='0' data-width='30' data-height='30' data-fgColor='#089de3' data-readOnly='1' data-bgColor='#3e4043'/>"
      + "<p></p><span class='icon'></span></li>";
    return item;
  }

  private void updateUploadForm() {
    String layout = getLayout();
    String uploadUrl = getUploadUrl();

    if (uploadUrl != null) {
      SafeHtml html = SafeHtmlUtils.fromSafeConstant("<form id='upload' method='post' action='" + uploadUrl
        + "' enctype='multipart/form-data'>" + "<div id='drop'><h4>" + messages.uploadPanelTextForLabelDropHere()
        + "</h4><a>" + messages.uploadPanelTextForLabelBrowseFiles() + "</a>" + "<input title='"
        + RodaConstants.API_PARAM_UPLOAD + "' type='file' name='" + "resource" + "' multiple='true' />" + " </div>"
        + "<input title='hiddenSubmit' type='submit' hidden/> </form>");

      uploadForm.setHTML(html);

      uploadForm.addDomHandler(event -> uploadForm.addStyleName(DRAGOVER), DragOverEvent.getType());

      uploadForm.addDomHandler(event -> uploadForm.removeStyleName(DRAGOVER), DragLeaveEvent.getType());

      uploadForm.addDomHandler(event -> uploadForm.removeStyleName(DRAGOVER), DropEvent.getType());

      JavascriptUtils.runMiniUploadForm(layout, new DefaultAsyncCallback<String>() {
        @Override
        public void onSuccess(String id) {
          Element item = Document.get().getElementById(id);
          String path = item.getAttribute("path");
          startItemLoadHandler(item);
          DatabaseService.Util.call((StringResponse newDatabaseUUID) -> {
            Toast.showInfo("SIARD created with success", PathUtils.getFileName(path));
            doneItemLoadHandler(item, "", newDatabaseUUID.getValue());
          }, (String errorMessage) -> {
            Dialogs.showErrors(messages.errorMessagesOpenFile(PathUtils.getFileName(path)), errorMessage,
              messages.basicActionClose());
            // Toast.showError("Cannot create SIARD", PathUtils.getFileName(path));
            item.addClassName("error");
            doneItemLoadHandler(item, errorMessage, null);
          }).create(path, ViewerConstants.SiardVersion.V2_1);
        }
      });
    } else {
      uploadForm.setHTML(SafeHtmlUtils.EMPTY_SAFE_HTML);
    }
  }

  private void startItemLoadHandler(Element item) {
    itemList.add(item);
    Element loadStatus = item.getElementsByTagName("span").getItem(0);
    loadStatus.addClassName("flash");
    loadStatus.setInnerText(messages.uploadPanelTextForLoading());
  }

  private void doneItemLoadHandler(Element item, String message, String databaseUUID) {
    Element loadStatus = item.getElementsByTagName("span").getItem(0);

    item.removeClassName("working");
    loadStatus.setInnerText(message);
    loadStatus.setAttribute("title", message);
    loadStatus.removeClassName("flash");
    if (databaseUUID != null) {
      Button btn = new Button();
      btn.setText(messages.uploadPanelTextForLabelGoToSIARDInfo());
      Element buttonElement = btn.getElement();
      buttonElement.addClassName("btn btn-goto-siard-info");

      Event.sinkEvents(buttonElement, Event.ONCLICK);
      Event.setEventListener(buttonElement, event -> {
        if (Event.ONCLICK == event.getTypeInt()) {
          HistoryManager.gotoSIARDInfo(databaseUUID);
        }

      });

      item.appendChild(buttonElement);
    }

    itemList.remove(item);
    if (itemList.isEmpty()) {
      uploadMessage.setVisible(true);
    }
  }
}