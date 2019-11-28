package com.databasepreservation.common.client.common.breadcrumb;

import java.util.List;
import java.util.Stack;

import com.databasepreservation.common.client.ClientLogger;
import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class BreadcrumbPanel extends FlowPanel {
  private ClientLogger logger = new ClientLogger(getClass().getName());

  private List<BreadcrumbItem> currentpath;

  private final Stack<Breadcrumb> breadcrumbs;

  public BreadcrumbPanel() {
    super();
    this.breadcrumbs = new Stack<Breadcrumb>();

    this.currentpath = null;

    // UserLogin.getInstance().addLoginStatusListener(new LoginStatusListener()
    // {
    // public void onLoginStatusChanged(RodaUser user) {
    // updatePath(currentpath);
    // }
    // });

    addStyleName("wui-breadcrumbPanel");

  }

  protected Widget createSeparator() {

//    final HTML separator = new HTML("&nbsp;/&nbsp;");
    final HTML separator = new HTML(SafeHtmlUtils.fromSafeConstant(FontAwesomeIconManager.getTag(FontAwesomeIconManager.BREADCRUMB_SEPARATOR)));
    separator.setStyleName("breadcrumb-separator");
    return separator;
  }

  /**
   * Update the breadcrumb panel
   *
   * @param path
   *          the new history path
   *
   */
  public void updatePath(List<BreadcrumbItem> path) {

    breadcrumbs.clear();
    for (final BreadcrumbItem item : path) {
      Breadcrumb breadcrumb = new Breadcrumb(item);

      if (item.getCommand() != null) {
        breadcrumb.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            item.getCommand().execute();
          }
        });
      }

      breadcrumbs.add(breadcrumb);
    }
    updateLayout();

    currentpath = path;
  }

  public void clear() {
    breadcrumbs.clear();
    updateLayout();
  }

  protected void updateLayout() {
    super.clear();
    for (int i = 0; i < breadcrumbs.size(); i++) {
      if (i > 0) {
        add(createSeparator());
      }
      Breadcrumb breadcrumb = breadcrumbs.get(i);
      breadcrumb.setLast(i == breadcrumbs.size() - 1);
      add(breadcrumb);
    }
  }

  protected class Breadcrumb extends HTML {

    private BreadcrumbItem item;

    private boolean enabled;

    private boolean last;

    public Breadcrumb(final BreadcrumbItem item) {
      super();
      setHTML(item.getLabel());

      this.item = item;
      enabled = true;
      last = true;

      addStyleName("breadcrumb");
    }

    /**
     * Is this breadcrumb enabled
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
      return enabled;
    }

    /**
     * Set if this breadcrumb is enabled. Overrides the click event
     *
     * @param enabled
     *
     */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    /**
     * Is this the last breadcrumb in list (current active breadcrumb)
     *
     * @return true if this breadcrumb is the last one. The last breadcrumb is
     *         disabled and has a different style.
     */
    public boolean isLast() {
      return last;
    }

    /**
     * Get the history path defined by this breadcrumb
     *
     * @return the breadcrumb history path
     */
    public BreadcrumbItem getItem() {
      return item;
    }

    /**
     * Get the last token on the history path defined by this breadcrumb
     *
     * @return the last history token
     */
    // public String getLastToken() {
    // return path[path.length - 1];
    // }

    /**
     * Set if this breadcrumb is the last one. The last breadcrumb will be disabled
     * and has a different style.
     *
     * @param last
     */
    public void setLast(boolean last) {
      this.last = last;
      this.setEnabled(!last);
      if (last) {
        this.addStyleName("breadcrumb-last");
      } else {
        this.removeStyleName("breadcrumb-last");
      }
    }

    public void onBrowserEvent(final Event event) {
      if (enabled) {
        super.onBrowserEvent(event);
      }
    }

    protected String getTargetHistoryToken(String[] path) {
      return null;// return tools.join(path, ".");
    }

  }
}
