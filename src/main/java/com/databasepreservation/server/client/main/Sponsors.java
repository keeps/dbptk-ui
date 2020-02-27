package com.databasepreservation.server.client.main;

import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.UserLogin;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.widgets.sponsors.SponsorWidget;
import com.databasepreservation.common.client.common.fields.MetadataField;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Sponsors extends ContentPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static ContributorsUiBinder uiBinder = GWT.create(ContributorsUiBinder.class);

  interface ContributorsUiBinder extends UiBinder<Widget, Sponsors> {
  }

  @UiField
  FlowPanel content;

  @UiField
  FlowPanel header;

  @UiField
  FlowPanel contributors;

  public Sponsors() {
    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  private void init() {
    header.add(CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.HANDS_HELP),
      messages.breadcrumbTextForSponsors(), "h1"));

    FlowPanel contributorsRow = CommonClientUtils.wrapOnDiv("contributors-row",
      CommonClientUtils.wrapOnDiv("contributors-column",
        new SponsorWidget("/img/DGLAB-logo.jpg", "http://dglab.gov.pt/")),
      CommonClientUtils.wrapOnDiv("contributors-column",
        new SponsorWidget("/img/e-ark-logo.png", "https://www.eark-project.com/")),
      CommonClientUtils.wrapOnDiv("contributors-column",
        new SponsorWidget("/img/rahvusarhiiv_3lovi_eng.png", "http://www.ra.ee/")),
      CommonClientUtils.wrapOnDiv("contributors-column", new SponsorWidget(
        "/img/eu_regional_development_fund_horizontal.jpg", "https://www.struktuurifondid.ee/et")));

    MetadataField instance = MetadataField.createInstance(messages.sponsorsPanelTextForDescription());
    instance.setCSS("table-row-description", "font-size-description");

    content.insert(instance, 1);

    contributors.add(contributorsRow);
  }

  @Override
  public void handleBreadcrumb(BreadcrumbPanel breadcrumb) {
    UserLogin.getInstance().getAuthenticatedUser(new DefaultAsyncCallback<User>() {
      @Override
      public void onSuccess(User user) {
        if (user.isGuest()) {
          BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forSponsorsGuest());
        } else {
          BreadcrumbManager.updateBreadcrumb(breadcrumb, BreadcrumbManager.forSponsors());
        }
      }
    });

  }
}
