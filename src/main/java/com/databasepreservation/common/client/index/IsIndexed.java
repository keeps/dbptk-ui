/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.index;

import com.databasepreservation.common.client.models.structure.ViewerDatabase;
import com.fasterxml.jackson.annotation.JsonSubTypes;

import java.io.Serializable;

/**
 * IsIndexed that is compatible with RODA objects, but made to fit DBVTK needs.
 * Unused methods are marked as deprecated.
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
@JsonSubTypes({@JsonSubTypes.Type(value= ViewerDatabase.class, name="ViewerDatabase")})
public abstract class IsIndexed implements Serializable {
  public abstract void setUuid(String uuid);

  public abstract String getUuid();
}
