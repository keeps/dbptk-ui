package com.databasepreservation.common.server.batch.core;

import java.util.List;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public interface JobDefinition {

  /**
   * Automatically generates the technical name of the Job (which typically
   * matches its Spring Bean name). By default, it uses the uncapitalized simple
   * class name.
   * <p>
   * It utilizes {@link ClassUtils#getUserClass(Class)} to safely resolve the real
   * class name, ensuring the correct name is generated even if Spring wraps the
   * bean in a CGLIB proxy during runtime.
   *
   * @return The internal programmatic name of the job.
   */
  default String getName() {
    Class<?> realClass = ClassUtils.getUserClass(this.getClass());
    return StringUtils.uncapitalize(realClass.getSimpleName());
  }

  /**
   * Retrieves the human-readable name of the Job intended for the user interface.
   * This name is displayed in the progress bar, alerts, and job history panels.
   *
   * @return The display name of the job (e.g., "Data Transformation").
   */
  String getDisplayName();

  /**
   * Provides the ordered sequence of steps that compose this job. The
   * orchestrator uses this list to build the execution flow. Steps will be
   * evaluated and executed in the exact order they are returned by this method,
   * provided their individual execution policies allow it.
   *
   * @return An ordered list of {@link StepDefinition}s.
   */
  List<StepDefinition> getSteps();

}
