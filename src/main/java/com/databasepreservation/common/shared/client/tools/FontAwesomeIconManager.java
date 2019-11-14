package com.databasepreservation.common.shared.client.tools;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import config.i18n.client.ClientMessages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class FontAwesomeIconManager {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);

  public static final String HOME = "home";
  public static final String LOGIN = "sign-in";
  public static final String DATABASES = "server";
  public static final String DATABASE = "database";
  public static final String SCHEMA = "cube";
  public static final String TABLE = "table";
  public static final String RECORD = "file";
  public static final String REFERENCE = "exchange";
  public static final String DATABASE_INFORMATION = "info-circle";
  public static final String DATABASE_USERS = "users";
  public static final String DATABASE_SEARCH = "search";
  public static final String DATABASE_REPORT = "clipboard";
  public static final String SCHEMA_STRUCTURE = "sitemap";
  public static final String SCHEMA_ROUTINES = "cog";
  public static final String SCHEMA_TRIGGERS = "clock-o";
  public static final String SCHEMA_VIEWS = "filter";
  public static final String SCHEMA_DATA = "th-large";
  public static final String SCHEMA_CHECK_CONSTRAINTS = "compress";
  public static final String SCHEMA_TABLE_SEPARATOR = "angle-right";
  public static final String SAVED_SEARCH = "save";
  public static final String ACTION_EDIT = "pencil-alt";
  public static final String ACTION_DELETE = "trash-alt";
  public static final String BLOB = "file";
  public static final String USER = "user";
  public static final String GLOBE = "globe";
  public static final String UPLOADS = "files-o";
  public static final String NEW_UPLOAD = "upload";
  public static final String LIST = "list";
  public static final String CALENDAR = "calendar";
  public static final String KEY = "key";
  public static final String SERVER = "server";
  public static final String QUESTION = "question-circle";
  public static final String SIARD_VALIDATIONS = "check";
  public static final String COG = "cog";
  public static final String SPIN = "spin";
  public static final String LOADING = "spinner";
  public static final String CHECK = "check-circle";
  public static final String TIMES = "times-circle";
  public static final String WARNING = "exclamation-triangle";
  public static final String SKIPPED = "forward";
  public static final String WIZARD = "magic";
  public static final String WRITE = "pencil";
  public static final String TECHNICAL = "briefcase";
  public static final String BREADCRUMB_SEPARATOR = "chevron-right";


  public static String getTag(String icon) {
    return "<i class=\"fa fa-" + icon + "\"></i>";
  }

  public static String getTagFW(String icon) {
    return "<i class=\"fa fa-fw fa-" + icon + "\"></i>";
  }

  public static String getTagRFW(String icon) {
    return "<i class=\"far fa-fw fa-" + icon + "\"></i>";
  }

  public static String getTagRegular(String icon) {
    return "<i class=\"far fa-" + icon + "\"></i>";
  }

  public static String getTagWithStyleName(String icon, String styleName) {
    return "<i class=\"fa fa-" + icon + " " + styleName +"\"></i>";
  }

  public static String getTag(String icon, String tooltip) {
    return "<i class=\"fa fa-" + icon + "\"" + " title = \"" + tooltip + "\"></i>";
  }

  public static SafeHtml loading(String icon) {
    return SafeHtmlUtils.fromSafeConstant(
      FontAwesomeIconManager.getTag(icon) + " <span class=\"loadingText\">" + messages.loading() + "</span>");
  }

  public static SafeHtml loaded(String icon, String text) {
    return SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(icon) + " " + SafeHtmlUtils.htmlEscape(text));
  }

  public static SafeHtml getTagSafeHtml(String icon, String text) {
    return getTagSafeHtml(icon, text, false);
  }

  public static SafeHtml getStackedIconSafeHtml(String iconButton, String iconTop, String text) {
    SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
    final String iButton = "<i class=\"fas fa-" + iconButton + " fa-stack-2x fa-fw\"></i>";
    final String iTop = "<i class=\"fas fa-" + iconTop + " fa-stack-1x fa-stack-right-corner\"></i>";

    safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant("<span class=\"fa-stack custom-views-stack\">"))
        .append(SafeHtmlUtils.fromSafeConstant(iButton))
        .append(SafeHtmlUtils.fromSafeConstant(iTop))
        .append(SafeHtmlUtils.fromSafeConstant("</span>"))
        .append(SafeHtmlUtils.fromSafeConstant(text));

    return safeHtmlBuilder.toSafeHtml();
  }

  public static SafeHtml getTagSafeHtml(String icon, String text, boolean regular) {
    SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
    if (regular) {
      return safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant(getTagRFW(icon))).append(SafeHtmlUtils.fromSafeConstant(text)).toSafeHtml();
    } else {
      return safeHtmlBuilder.append(SafeHtmlUtils.fromSafeConstant(getTagFW(icon))).append(SafeHtmlUtils.fromSafeConstant(text)).toSafeHtml();
    }
  }
}
