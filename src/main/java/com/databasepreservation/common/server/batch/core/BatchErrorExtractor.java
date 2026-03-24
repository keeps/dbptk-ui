package com.databasepreservation.common.server.batch.core;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class BatchErrorExtractor {
  private BatchErrorExtractor() {
  }

  /**
   * Unpacks Spring Batch wrapper exceptions to find the most meaningful business
   * message and combines it with the absolute root cause (e.g., I/O errors).
   */
  public static String extractMeaningfulError(Throwable ex) {
    if (ex == null)
      return "Unknown error";

    Throwable current = ex;
    Throwable rootCause = ex;
    String mostMeaningfulMessage = ex.getMessage();

    // Traverse the stack to find the root cause and bypass Spring Batch generic
    // wrappers
    while (current != null) {
      rootCause = current;
      String msg = current.getMessage();

      // If we find a business exception, it's more meaningful than Spring Batch's
      // wrappers
      if (msg != null && !isGenericFrameworkMessage(msg)) {
        if (isGenericFrameworkMessage(mostMeaningfulMessage)) {
          mostMeaningfulMessage = msg;
        }
      }
      current = current.getCause();
    }

    String rootMessage = rootCause.getMessage() != null ? rootCause.getMessage() : rootCause.getClass().getSimpleName();

    // If the most meaningful message is different from the absolute root cause
    // (e.g. "Tika Extraction Failed" vs "Connection Refused"), we combine them for
    // context.
    if (mostMeaningfulMessage != null && !mostMeaningfulMessage.equals(rootMessage)
      && !isGenericFrameworkMessage(mostMeaningfulMessage)) {
      return mostMeaningfulMessage + " (Root cause: " + rootMessage + ")";
    }

    return rootMessage;
  }

  private static boolean isGenericFrameworkMessage(String msg) {
    if (msg == null)
      return true;
    return msg.contains("Non-skippable exception") || msg.contains("Partition handler returned an unsuccessful step")
      || msg.contains("Encountered an error executing step") || msg.contains("Unexpected exception while processing");
  }
}
