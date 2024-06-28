/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.utils;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Provider
public class CacheFilterFactory implements DynamicFeature {

  private static final CacheResponseFilter NO_CACHE_FILTER =
      new CacheResponseFilter("no-cache");

  @Override
  public void configure(ResourceInfo resourceInfo,
                        FeatureContext featureContext) {

    CacheControlHeader cch = resourceInfo.getResourceMethod()
        .getAnnotation(CacheControlHeader.class);
    if (cch == null) {
      featureContext.register(NO_CACHE_FILTER);
    } else {
      featureContext.register(new CacheResponseFilter(cch.value()));
    }
  }

  private static class CacheResponseFilter implements ContainerResponseFilter {
    private final String headerValue;

    CacheResponseFilter(String headerValue) {
      this.headerValue = headerValue;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext,
                       ContainerResponseContext containerResponseContext) {
      // attache Cache Control header to each response
      // based on the annotation value
      if (!containerResponseContext.getHeaders().containsKey(HttpHeaders.CACHE_CONTROL)) {
        containerResponseContext
            .getHeaders()
            .putSingle(HttpHeaders.CACHE_CONTROL, headerValue);

        if (headerValue != null && headerValue.equals("no-cache")) {
          containerResponseContext
              .getHeaders()
              .putSingle("Pragma", headerValue);
          containerResponseContext.getHeaders().putSingle(HttpHeaders.EXPIRES, -1);
        }
      }
    }
  }
}
