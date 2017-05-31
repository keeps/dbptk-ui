/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.visualization.shared.client.widgets.wcag;

import org.roda.core.data.exceptions.GenericException;

import com.databasepreservation.visualization.client.common.utils.JavascriptUtils;
import com.databasepreservation.visualization.shared.client.ClientLogger;
import com.databasepreservation.visualization.shared.client.Tools.RestUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author Luís Faria
 * @author Bruno Ferreira
 * 
 */
public class MarkdownWidgetWrapper extends HTML {

  private ClientLogger logger = new ClientLogger(getClass().getName());

  public MarkdownWidgetWrapper(String resourceId) {
    this(resourceId, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        // do nothing
      }

      @Override
      public void onSuccess(Void result) {
        // do nothing
      }
    });
  }

  public MarkdownWidgetWrapper(String databaseUUID, final AsyncCallback<Void> callback) {
    RequestBuilder request = new RequestBuilder(RequestBuilder.GET, RestUtils.createReportResourceUri(databaseUUID)
      .asString());

    try {
      request.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == 200) {
            MarkdownWidgetWrapper.this.setContentFromMarkdown(response.getText());
            callback.onSuccess(null);
          } else {
            MarkdownWidgetWrapper.this.setErrorMessage("Unable to retrieve the report file");
            logger.error("Unable to retrieve the report file");
            callback.onFailure(new GenericException("Unable to retrieve the report file"));
          }
        }

        @Override
        public void onError(Request request, Throwable exception) {
          logger.error("Unable to retrieve the report file", exception);
          MarkdownWidgetWrapper.this.setErrorMessage("Unable to retrieve the report file");
          callback.onFailure(exception);
        }
      });
    } catch (RequestException exception) {
      MarkdownWidgetWrapper.this.setErrorMessage("Error sending request");
      logger.error("Error sending request", exception);
    }
  }

  public void setContentFromMarkdown(String markdownText) {
    String htmlText = JavascriptUtils.convertMarkdownToHTML(markdownText);
    setHTML(htmlText);
  }

  public void setErrorMessage(String reason) {
    setText(reason);
  }
}
