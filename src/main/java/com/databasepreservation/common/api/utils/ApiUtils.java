/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.roda.core.data.common.RodaConstants;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.databasepreservation.common.server.storage.BinaryConsumesOutputStream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.StreamingOutput;

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
        mediaType = MediaType.APPLICATION_XML_VALUE;
      } else if (acceptFormat.equalsIgnoreCase("JSONP")) {
        mediaType = APPLICATION_JS;
      } else if (acceptFormat.equalsIgnoreCase("bin")) {
        mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
      } else if (acceptFormat.equalsIgnoreCase("html")) {
        mediaType = MediaType.TEXT_HTML_VALUE;
      }
    } else if (StringUtils.isNotBlank(acceptHeaders)) {
      if (acceptHeaders.contains(MediaType.APPLICATION_XML_VALUE)) {
        mediaType = MediaType.APPLICATION_XML_VALUE;
      } else if (acceptHeaders.contains(APPLICATION_JS)) {
        mediaType = APPLICATION_JS;
      } else if (acceptHeaders.contains(ExtraMediaType.TEXT_CSV)) {
        mediaType = ExtraMediaType.TEXT_CSV;
      }
    }

    return mediaType;
  }

  public static ResponseEntity<StreamingResponseBody> rangeResponse(HttpHeaders headers, BinaryConsumesOutputStream streamResponse) {

    final HttpHeaders responseHeaders = new HttpHeaders();

    HttpRange range = headers.getRange().get(0);
    long start = range.getRangeStart(streamResponse.getSize());
    long end = range.getRangeEnd(streamResponse.getSize());

    String contentLength = String.valueOf((end - start) + 1);
    responseHeaders.add(HttpHeaders.CONTENT_TYPE, streamResponse.getMediaType());
    responseHeaders.add(HttpHeaders.CONTENT_LENGTH, contentLength);
    responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
      "inline; filename=\"" + streamResponse.getFileName() + "\"");
    responseHeaders.add(HttpHeaders.ACCEPT_RANGES, "bytes");
    responseHeaders.add(HttpHeaders.CONTENT_RANGE,
      "bytes" + " " + start + "-" + end + "/" + streamResponse.getSize());

    StreamingResponseBody responseStream = os -> streamResponse.consumeOutputStream(os, start, end);

    Date lastModifiedDate = streamResponse.getLastModified();
    if (lastModifiedDate != null) {
      CacheControl cacheControl = CacheControl.empty().cachePrivate().sMaxAge(Duration.ofSeconds(60));
      responseHeaders.add(HttpHeaders.CACHE_CONTROL, cacheControl.getHeaderValue());
      responseHeaders.setETag(Long.toString(lastModifiedDate.getTime()));
      responseHeaders.add(HttpHeaders.LAST_MODIFIED, streamResponse.getLastModified().toString());
    }

    return new ResponseEntity<>(responseStream, responseHeaders, HttpStatus.PARTIAL_CONTENT);
  }

  public static ResponseEntity<StreamingResponseBody> okResponse(StreamResponse streamResponse) {
    return okResponse(streamResponse, false);
  }

  public static ResponseEntity<StreamingResponseBody> okResponse(StreamResponse streamResponse, boolean inline) {
    StreamingOutput so = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws IOException {
        streamResponse.getStream().consumeOutputStream(output);
      }
    };

    HttpHeaders responseHeaders = new HttpHeaders();
    StreamingResponseBody responseStream = outputStream -> streamResponse.getStream().consumeOutputStream(outputStream);

    responseHeaders.add("Content-Type", streamResponse.getStream().getMediaType());
    responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION,
      contentDisposition(inline) + CONTENT_DISPOSITION_FILENAME_ARGUMENT + "\"" + streamResponse.getFilename() + "\"");
    responseHeaders.add("Content-Length", String.valueOf(streamResponse.getStream().getSize()));

    Date lastModifiedDate = streamResponse.getStream().getLastModified();

    if (lastModifiedDate != null) {
      CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate().noTransform();
      String eTag = lastModifiedDate.toInstant().toString();
      return ResponseEntity.ok().headers(responseHeaders).cacheControl(cacheControl).eTag(eTag).body(responseStream);
    }

    return ResponseEntity.ok().headers(responseHeaders).body(responseStream);
  }

  private static String contentDisposition(boolean inline) {
    return inline ? CONTENT_DISPOSITION_INLINE : CONTENT_DISPOSITION_ATTACHMENT;
  }

  public static ResponseEntity<StreamingResponseBody> okResponse(StreamResponse streamResponse, WebRequest request) {
    if (request != null && request.checkNotModified(streamResponse.getLastModified().getTime())) {
      return ResponseEntity.status(304).build();
    }

    org.springframework.http.HttpHeaders responseHeaders = new org.springframework.http.HttpHeaders();
    StreamingResponseBody responseStream = outputStream -> streamResponse.getStream().consumeOutputStream(outputStream);

    responseHeaders.add("Content-Type", streamResponse.getStream().getMediaType());
    responseHeaders.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
      "attachment; filename=\"" + streamResponse.getStream().getFileName() + "\"");
       responseHeaders.add("Content-Length",
       String.valueOf(streamResponse.getStream().getSize()));

    Date lastModifiedDate = streamResponse.getStream().getLastModified();

    if (lastModifiedDate != null) {
      org.springframework.http.CacheControl cacheControl = org.springframework.http.CacheControl
        .maxAge(1, TimeUnit.HOURS).cachePrivate().noTransform();
      String eTag = lastModifiedDate.toInstant().toString();
      return ResponseEntity.ok().headers(responseHeaders).cacheControl(cacheControl).eTag(eTag).body(responseStream);
    }

    return ResponseEntity.ok().headers(responseHeaders).body(responseStream);
  }
}