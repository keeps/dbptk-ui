package com.databasepreservation.server.client.browse;

import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.server.client.browse.upload.SIARDUpload;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class UploadPanel extends ContentPanel {
  interface NewUploadPanelUiBinder extends UiBinder<Widget, UploadPanel> {
  }

  private static UploadPanel instance = null;

  public static UploadPanel getInstance() {
    if (instance == null) {
      instance = new UploadPanel();
    }
    return instance;
  }

  private static NewUploadPanelUiBinder uiBinder = GWT.create(NewUploadPanelUiBinder.class);

  @UiField
  FlowPanel content;

  @UiField
  FlowPanel header;

  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private UploadPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  /**
   * Uses BreadcrumbManager to show available information in the breadcrumbPanel
   *
   * @param breadcrumb
   *          the BreadcrumbPanel for this database
   */
  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forUpload());
  }

  private void init() {
    header.add(CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.NEW_UPLOAD),
        messages.uploadPanelTextForTitle(), "h1"));

    MetadataField instance = MetadataField
        .createInstance(messages.uploadPanelTextForDescription());
    instance.setCSS("table-row-description", "font-size-description");

    content.add(instance);
    content.add(SIARDUpload.getInstance());
  }

}
