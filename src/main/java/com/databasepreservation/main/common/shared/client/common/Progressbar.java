package com.databasepreservation.main.common.shared.client.common;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Progressbar extends HTMLPanel {
  SimplePanel progressBarDiv;
  InlineHTML progressBarSpan;

  private int value = 0;
  private int maximum = 100;

  public Progressbar(String html) {
    this();
  }

  public Progressbar(SafeHtml safeHtml) {
    this();
  }

  public Progressbar(String tag, String html) {
    this();
  }

  public Progressbar() {
    super("progress", null);
    addStyleName("html5progress");

    progressBarDiv = new SimplePanel();
    progressBarDiv.addStyleName("progress-bar");
    progressBarSpan = new InlineHTML();

    setMaximum(maximum);
    // current is set right after maximum
  }

  public Progressbar(int maximum) {
    this();
    setMaximum(maximum);
  }

  public Progressbar(int current, int maximum) {
    this(maximum);
    setCurrent(current);
  }

  public void setCurrent(int value) {
    this.value = value;

    // set html5 version value
    getElement().setAttribute("value", String.valueOf(value));

    // set non-html5 version value
    int progress = 0;
    if (maximum >= 0 && value > 0) {
      progress = (int) ((value * 1.0D) / maximum) * 100;
    }
    String asString = String.valueOf(progress);
    progressBarSpan.setWidth(asString);
    progressBarSpan.setText(asString + "%");
  }

  public void setMaximum(int maximum) {
    getElement().setAttribute("max", String.valueOf(maximum));

    // set current again, since non-html is relative to the maximum
    setCurrent(value);
  }
}
