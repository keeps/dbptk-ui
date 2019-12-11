package com.databasepreservation.common.utils;

import java.lang.reflect.Method;
import java.util.Date;

import org.roda.core.data.v2.user.User;

import com.databasepreservation.common.client.models.activity.logs.LogEntryState;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class ControllerAssistant {
  private final Date startDate;
  private final Method enclosingMethod;

  public ControllerAssistant() {
    this.startDate = new Date();
    this.enclosingMethod = this.getClass().getEnclosingMethod();
  }

  public void registerAction(final User user, final String relatedObjectId, final LogEntryState state,
    final Object... parameters) {
    final long duration = new Date().getTime() - startDate.getTime();
    ControllerAssistantUtils.registerAction(user, this.enclosingMethod.getDeclaringClass().getName(),
      this.enclosingMethod.getName(), relatedObjectId, duration, state, parameters);
  }

  public void registerAction(final User user, final LogEntryState state, final Object... parameters) {
    registerAction(user, null, state, parameters);
  }

  public void registerAction(final User user, final LogEntryState state) {
    registerAction(user, (String) null, state);
  }
}
