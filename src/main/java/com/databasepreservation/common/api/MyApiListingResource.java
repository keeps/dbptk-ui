/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.api;

import io.swagger.jaxrs.listing.ApiListingResource;

import javax.ws.rs.Path;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path("/api/swagger.{type:json|yaml}")
public class MyApiListingResource extends ApiListingResource {

}
