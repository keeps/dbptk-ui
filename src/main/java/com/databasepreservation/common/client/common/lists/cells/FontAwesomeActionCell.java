package com.databasepreservation.common.client.common.lists.cells;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

import com.databasepreservation.common.client.tools.FontAwesomeIconManager;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Based on com.google.gwt.cell.client.ActionCell and adapted to create a
 * bootstrap button with custom css style and FontAwesome icons
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class FontAwesomeActionCell<C> extends AbstractCell<C> {

  /**
   * The delegate that will handle events from the cell.
   *
   * @param <T>
   *          the type that this delegate acts on
   */
  public interface Delegate<T> {
    /**
     * Perform the desired action on the given object.
     *
     * @param object
     *          the object to be acted upon
     */
    void execute(T object);
  }

  private final SafeHtml html;
  private final Delegate<C> delegate;

  /**
   * Construct a new {@link FontAwesomeActionCell}.
   *
   * @param tooltip
   *          the tooltip to display on the button
   * @param icon
   *          the icon to display in the button
   * @param extraButtonClasses
   *          css classes to be added to the <button>
   * @param delegate
   *          the delegate that will handle events
   */
  public FontAwesomeActionCell(String tooltip, String icon, String extraButtonClasses, Delegate<C> delegate) {
    super(CLICK, KEYDOWN);
    this.delegate = delegate;

    if (extraButtonClasses == null) {
      extraButtonClasses = "";
    } else {
      extraButtonClasses = " " + extraButtonClasses;
    }

    this.html = SafeHtmlUtils.fromSafeConstant(
      "<button type=\"button\" tabindex=\"-1\" class=\"btn btn-cell-action" + extraButtonClasses + "\" title=\""
        + SafeHtmlUtils.htmlEscape(tooltip) + "\">" + FontAwesomeIconManager.getTag(icon) + "</button>");
  }

  /**
   * Construct a new {@link FontAwesomeActionCell}.
   *
   * @param tooltip
   *          the tooltip to display on the button
   * @param icon
   *          the icon to display in the button
   * @param delegate
   *          the delegate that will handle events
   */
  public FontAwesomeActionCell(String tooltip, String icon, Delegate<C> delegate) {
    this(tooltip, icon, null, delegate);
  }

  @Override
  public void onBrowserEvent(Context context, Element parent, C value, NativeEvent event,
    ValueUpdater<C> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    if (CLICK.equals(event.getType())) {
      EventTarget eventTarget = event.getEventTarget();
      if (!Element.is(eventTarget)) {
        return;
      }
      if (parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
        // Ignore clicks that occur outside of the main element.
        onEnterKeyDown(context, parent, value, event, valueUpdater);
      }
    }
  }

  @Override
  public void render(Context context, C value, SafeHtmlBuilder sb) {
    sb.append(html);
  }

  @Override
  protected void onEnterKeyDown(Context context, Element parent, C value, NativeEvent event,
    ValueUpdater<C> valueUpdater) {
    delegate.execute(value);
  }
}
