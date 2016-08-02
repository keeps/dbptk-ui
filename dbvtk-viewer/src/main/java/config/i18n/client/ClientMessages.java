package config.i18n.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public interface ClientMessages extends Messages {
  String browserOfflineError();

  String cannotReachServerError();

  String alertErrorTitle();

  String executingTaskMessage();
}
