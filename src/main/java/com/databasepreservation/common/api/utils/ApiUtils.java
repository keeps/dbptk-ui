/**
 * The contents of this file are based on those found at https://github.com/keeps/roda
 * and are subject to the license and copyright detailed in https://github.com/keeps/roda
 */
package com.databasepreservation.common.api.utils;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.common.RodaConstants;

/**
 * API Utils
 *
 * @author Hélder Silva <hsilva@keep.pt>
 */
public class ApiUtils {

  private static final String CONTENT_DISPOSITION_FILENAME_ARGUMENT = "filename=";
  private static final String CONTENT_DISPOSITION_INLINE = "inline; ";
  private static final String CONTENT_DISPOSITION_ATTACHMENT = "attachment; ";

  /**
   * Get media type
   *
   * @param acceptFormat
   *          String with required format
   * @param request
   *          http request
   * @return media type
   */
  public static String getMediaType(String acceptFormat, HttpServletRequest request) {
    return getMediaType(acceptFormat, request.getHeader(RodaConstants.API_HTTP_HEADER_ACCEPT));
  }

  /**
   * Get media type
   *
   * @param acceptFormat
   *          String with required format
   * @param acceptHeaders
   *          String with request headers
   * @return media type
   */
  public static String getMediaType(final String acceptFormat, final String acceptHeaders) {
    final String APPLICATION_JS = "application/javascript; charset=UTF-8";

    String mediaType = MediaType.APPLICATION_JSON + "; charset=UTF-8";

    if (StringUtils.isNotBlank(acceptFormat)) {
      if (acceptFormat.equalsIgnoreCase("XML")) {
        mediaType = MediaType.APPLICATION_XML;
      } else if (acceptFormat.equalsIgnoreCase("JSONP")) {
        mediaType = APPLICATION_JS;
      } else if (acceptFormat.equalsIgnoreCase("bin")) {
        mediaType = MediaType.APPLICATION_OCTET_STREAM;
      } else if (acceptFormat.equalsIgnoreCase("html")) {
        mediaType = MediaType.TEXT_HTML;
      }
    } else if (StringUtils.isNotBlank(acceptHeaders)) {
      if (acceptHeaders.contains(MediaType.APPLICATION_XML)) {
        mediaType = MediaType.APPLICATION_XML;
      } else if (acceptHeaders.contains(APPLICATION_JS)) {
        mediaType = APPLICATION_JS;
      } else if (acceptHeaders.contains(ExtraMediaType.TEXT_CSV)) {
        mediaType = ExtraMediaType.TEXT_CSV;
      }
    }

    return mediaType;
  }

  public static Response okResponse(StreamResponse streamResponse, CacheControl cacheControl, EntityTag tag,
    boolean inline) {
    StreamingOutput so = new StreamingOutput() {

      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        streamResponse.getStream().consumeOutputStream(output);

      }
    };
    return Response.ok(so, streamResponse.getMediaType())
      .header(HttpHeaders.CONTENT_DISPOSITION,
        contentDisposition(inline) + CONTENT_DISPOSITION_FILENAME_ARGUMENT + "\"" + streamResponse.getFilename() + "\"")
      .cacheControl(cacheControl).tag(tag).build();
  }

  public static Response okResponse(StreamResponse streamResponse) {
    return okResponse(streamResponse, false);
  }

  public static Response okResponse(StreamResponse streamResponse, boolean inline) {
    StreamingOutput so = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        streamResponse.getStream().consumeOutputStream(output);
      }
    };
    return Response.ok(so, streamResponse.getMediaType())
      .header(HttpHeaders.CONTENT_DISPOSITION,
        contentDisposition(inline) + CONTENT_DISPOSITION_FILENAME_ARGUMENT + "\"" + streamResponse.getFilename() + "\"")
      .build();
  }

  private static String contentDisposition(boolean inline) {
    return inline ? CONTENT_DISPOSITION_INLINE : CONTENT_DISPOSITION_ATTACHMENT;
  }
}
