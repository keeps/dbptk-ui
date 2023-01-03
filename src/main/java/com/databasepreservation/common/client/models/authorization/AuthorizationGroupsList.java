/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.client.models.authorization;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AuthorizationGroupsList implements Serializable {
  private static final long serialVersionUID = -3730186735554294942L;

  private Set<AuthorizationGroup> authorizationGroupList = new HashSet<>();

  public Set<AuthorizationGroup> getAuthorizationGroupsList() {
    return authorizationGroupList;
  }

  @JsonIgnore
  public AuthorizationGroup get(String permission) {
    for (AuthorizationGroup authorizationGroup : authorizationGroupList) {
      if (authorizationGroup.getAttributeValue().equals(permission)) {
        return authorizationGroup;
      }
    }
    return null;
  }

  @JsonIgnore
  public void add(AuthorizationGroup authorizationGroup) {
    this.authorizationGroupList.add(authorizationGroup);
  }
}
