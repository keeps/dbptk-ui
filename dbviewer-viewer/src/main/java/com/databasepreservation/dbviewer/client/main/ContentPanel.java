package com.databasepreservation.dbviewer.client.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.databasepreservation.dbviewer.shared.client.widgets.wcag.AccessibleFocusPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class ContentPanel extends Composite {
  interface ContentPanelUiBinder extends UiBinder<Widget, ContentPanel> {
  }

  private static ContentPanelUiBinder binder = GWT.create(ContentPanelUiBinder.class);

  @UiField
  AccessibleFocusPanel searchInputButton;
  @UiField
  FlowPanel sidebar;
  @UiField
  TextBox searchInputBox;
  @UiField
  BreadcrumbPanel breadcrumb;
  @UiField
  FlowPanel content;
  @UiField
  FlowPanel sidebarContent;
  @UiField
  HorizontalPanel previewPanel;
  @UiField
  FocusPanel focusPanel;
  @UiField
  FlowPanel filePreviewPanel;

  public void updateContent(Widget w) {
    content.clear();
    content.add(w);
  }

  public ContentPanel() {
    initWidget(binder.createAndBindUi(this));

    content.clear();
    content.add(new HTML(new SafeHtml() {
      @Override
      public String asString() {
        return "<span>content goes here</span>";
      }
    }));

    sidebarContent.clear();
    sidebarContent.add(new HTML(new SafeHtml() {
      @Override
      public String asString() {
        return "<span>list here</span>";
      }
    }));

    List<BreadcrumbItem> bclist = new ArrayList<>();
    bclist.add(new BreadcrumbItem("Databases", Arrays.asList("#")));
    bclist.add(new BreadcrumbItem("Something", Arrays.asList("#", "1")));
    breadcrumb.updatePath(bclist);
    breadcrumb.setVisible(true);

    searchInputBox.getElement().setPropertyString("placeholder", "search...");

    focusPanel.addStyleName("viewRepresentationFocusPanel");
    previewPanel.addStyleName("viewRepresentationPreviewPanel");
    sidebar.addStyleName("viewRepresentationFilesPanel");
    filePreviewPanel.addStyleName("viewRepresentationFilePreviewPanel");
    // sidebar.addStyleName("viewRepresentationFilePreview");
    previewPanel.setCellWidth(filePreviewPanel, "100%");
  }
}
