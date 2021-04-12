/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.desktop.client.common;

import com.google.gwt.core.client.GWT;
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