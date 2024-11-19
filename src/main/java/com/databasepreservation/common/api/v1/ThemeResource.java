/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api.v1;

import java.io.IOException;
import java.io.InputStream;

import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.common.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.databasepreservation.common.api.exceptions.RESTException;
import com.databasepreservation.common.api.utils.ApiUtils;
import com.databasepreservation.common.api.utils.Theme;
import com.databasepreservation.common.client.ViewerConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(path = ThemeResource.ENDPOINT)
@Tag(name = ThemeResource.SWAGGER_ENDPOINT)
public class ThemeResource {
  public static final String ENDPOINT = "/" + ViewerConstants.API_SERVLET + ViewerConstants.API_V1_THEME_RESOURCE;
  public static final String SWAGGER_ENDPOINT = "v1 theme";
  public static final int CACHE_CONTROL_MAX_AGE = 60;

  @GetMapping()
  @Operation(summary = "Gets the custom theme")
  public ResponseEntity<StreamingResponseBody> getResource(
    @Parameter(name = "The resource id", required = true) @RequestParam(ViewerConstants.API_QUERY_PARAM_RESOURCE_ID) String resourceId,
    @Parameter(name = "The default resource id", required = false) @RequestParam(value = ViewerConstants.API_QUERY_PARAM_DEFAULT_RESOURCE_ID, required = false) String fallbackResourceId,
    @Parameter(name = "If the resource is served inline", required = false) @RequestParam(value = ViewerConstants.API_QUERY_PARAM_INLINE, required = false) boolean inline,
    WebRequest request) throws IOException, NotFoundException {

    Pair<String, InputStream> themeResource = Theme.getThemeResource(resourceId, fallbackResourceId);

    if (themeResource.getSecond() != null) {
      return ApiUtils.okResponse(Theme.getThemeResourceStreamResponse(themeResource), request);
    } else {
      throw new RESTException(new NotFoundException("File not found: " + resourceId));
    }
  }
}
