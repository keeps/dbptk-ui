package com.databasepreservation.common.api;

import io.swagger.jaxrs.listing.ApiListingResource;

import javax.ws.rs.Path;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
@Path("/api/swagger.{type:json|yaml}")
public class MyApiListingResource extends ApiListingResource {

}
