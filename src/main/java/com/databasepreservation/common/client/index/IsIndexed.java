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
