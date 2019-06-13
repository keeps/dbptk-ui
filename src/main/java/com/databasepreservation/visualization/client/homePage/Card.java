package com.databasepreservation.visualization.client.homePage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class Card extends Composite {

  public static Card createInstance(String header, String about, Button button) {
    return new Card(header, about, button);
  }

  interface cardsUiBinder extends UiBinder<Widget, Card> {
  }

  private static cardsUiBinder binder = GWT.create(cardsUiBinder.class);

  @UiField
  FlowPanel header, text, buttonHolder;

  private Card(String headerTxt, String aboutTxt, Button button) {
    initWidget(binder.createAndBindUi(this));

    HTML headingElement = new HTML();
    headingElement.setHTML("<h3>" + headerTxt + "</h3>");

    HTML aboutElement = new HTML();
    aboutElement.setHTML("<p>" + aboutTxt + "</p>");


    header.add(headingElement);
    text.add(aboutElement);
    buttonHolder.add(button);
  }
}