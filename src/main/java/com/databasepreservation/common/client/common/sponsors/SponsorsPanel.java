package com.databasepreservation.common.client.common.sponsors;

import com.databasepreservation.common.client.common.ContentPanel;
import com.databasepreservation.common.client.common.DefaultAsyncCallback;
import com.databasepreservation.common.client.common.UserLogin;
import com.databasepreservation.common.client.common.breadcrumb.BreadcrumbPanel;
import com.databasepreservation.common.client.common.utils.CommonClientUtils;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.client.tools.BreadcrumbManager;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.databasepreservation.common.client.widgets.sponsors.SponsorWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SponsorsPanel extends ContentPanel {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  private static ContributorsUiBinder uiBinder = GWT.create(ContributorsUiBinder.class);

  interface ContributorsUiBinder extends UiBinder<Widget, SponsorsPanel> {
  }

  @UiField
  FlowPanel content;

  public SponsorsPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    init();
  }

  private void init() {
    content.add(CommonClientUtils.getHeaderHTML(FontAwesomeIconManager.getTag(FontAwesomeIconManager.HANDS_HELP),
      messages.breadcrumbTextForSponsors(), "h1"));

    final String height = "100px";
    final SponsorWidget poap = new SponsorWidget("/img/sponsors/POAP.jpeg", null, height);
    final SponsorWidget dglab = new SponsorWidget("/img/sponsors/DGLAB.jpg", "http://dglab.gov.pt/", height);
    final SponsorWidget cip = new SponsorWidget("/img/sponsors/CIP.jpg", "https://ec.europa.eu/cip/", height);
    final SponsorWidget eark = new SponsorWidget("/img/sponsors/E-ARK.png", "https://www.eark-project.com/", height);
    final SponsorWidget cef = new SponsorWidget("/img/sponsors/CEF.png",
      "https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL", null);
    final SponsorWidget eark4all = new SponsorWidget("/img/sponsors/E-ARK4ALL.jpg", "https://e-ark4all.eu/", height);
    final SponsorWidget naeFeder = new SponsorWidget("/img/sponsors/NAE-FEDER.png",
      "https://www.struktuurifondid.ee/et", height);
    final SponsorWidget nae = new SponsorWidget("/img/sponsors/NAE.png", "http://www.ra.ee/", height);

    content.add(CommonClientUtils.constructParagraph(
      "The development of DBPTK started in 2006. At an early stage the project was sponsored by the Directorate-General of Portuguese Archives (DGARQ) and financially supported by the Community Support Framework - POAP (grant agreement no 000613/2006/111). The initial purpose of the project was to build a functioning prototype of a digital repository that would lead to more concrete ideas and knowledge on how to build an operational digital preservation repository for DGARQ. As part of that project the first version of DBPTK was developed. At the time the tool was embedded in RODA-in, a SIP creation tool."));

    content.add(CommonClientUtils.constructParagraph(
      "Between 2014 and 2016 DBPTK has received financial and technical support from the E-ARK project (co-funded by KEEP SOLUTIONS and the European Commission Competiveness and Innovation Framework Programme, grant agreement no 620998). In this project DBPTK has been enhanced to support more archival formats such as SIARD and SIARD DK, and also to include a visualization component for SIARD files. The tool has been released as an independent tool under its own branding."));

    content.add(CommonClientUtils.constructParagraph(
      "In 2018 the product continued to be developed under the project EARK4ALL CEF eArchiving Building Block.\nIn 2019 the product received special funding from the Estonian National Archives, co-funded by the European Regional Development Fund of the European Union, and a new major version has been released to meet the special requirements of the Estonian and Danish National Archives.\nDuring 2020 and 2021, the product continued to be enhanced under the auspices of the CEF eArchiving Building Block."));

    content.add(CommonClientUtils
      .constructParagraph("Till this day, the product continues to be maintained and supported by KEEP SOLUTIONS."));

    content.add(new HTMLPanel(HeadingElement.TAG_H2, "Special thanks"));

    content.add(CommonClientUtils.constructParagraph(
      "Throughout the years, DBPTK has received contributions from various individuals that have enhanced the product in many different ways (ideas, bug fixes, features, translations, etc.). "));

    content.add(CommonClientUtils.constructParagraph(
      "A special thanks goes to José Carlos Ramalho from the University of Minho for his contribution to the body of knowledge that instigated the development of DBPTK, including the development of the first version of an XML-based database archival format (DBML); Luís Faria from KEEP SOLUTIONS for managing the roadmap of the product over the years and for kicking of the development of the first version; Bruno Ferreira for its contributions to the SIARD 2.0 specification and its implementation in code; Miguel Ferreira for its lateral contributions to the vision and quality assessment of the tool; Miguel Guimarães and Gabriel Barros, both from KEEP SOLUTIONS for developing one of the largest releases of the product; Kuldar Aas and Lauri Rätsep from the Estonian National Archives for all the ideas, funding and quality control; Anders Bo Nielsen and Philip Tømmerholt from the Danish National Archives for their contribution on testing and steering the tool roadmap; Czech Republic National Archives for translating the DBPTK into Czech and German and all others that have directly or indirectly supported the project over the years."));

    content.add(new HTMLPanel(HeadingElement.TAG_H2, "Sponsored by"));

    content.add(CommonClientUtils.wrapOnDiv("center",
      CommonClientUtils.wrapOnDiv("sponsors", CommonClientUtils.wrapOnDiv("sponsor", poap),
        CommonClientUtils.wrapOnDiv("sponsor", dglab), CommonClientUtils.wrapOnDiv("sponsor", cip),
        CommonClientUtils.wrapOnDiv("sponsor", eark)),
      CommonClientUtils.wrapOnDiv("sponsors", CommonClientUtils.wrapOnDiv("sponsor", cef),
        CommonClientUtils.wrapOnDiv("sponsor", eark4all), CommonClientUtils.wrapOnDiv("sponsor", naeFeder),
        CommonClientUtils.wrapOnDiv("sponsor", nae))));

    content.add(new HTMLPanel(HeadingElement.TAG_H2, "Developed by"));

    final SponsorWidget keep = new SponsorWidget("/img/sponsors/KEEP.svg", "https://keep.pt", "100px");

    content.add(CommonClientUtils.wrapOnDiv("sponsors", CommonClientUtils.wrapOnDiv("sponsor", keep)));
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
