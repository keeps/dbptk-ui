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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AuthorizationGroupsList implements Serializable {
  private static final long serialVersionUID = -3730186735554294942L;

  private Set<AuthorizationGroups> authorizationGroupsList = new HashSet<>();

  public Set<AuthorizationGroups> getAuthorizationGroupsList() {
    return authorizationGroupsList;
  }

  @JsonIgnore
  public AuthorizationGroups get(String permission) {
    for (AuthorizationGroups authorizationGroups : authorizationGroupsList) {
      if (authorizationGroups.getAttributeValue().equals(permission)) {
        return authorizationGroups;
      }
    }
    return null;
  }

  @JsonIgnore
  public List<String> getAllAttributeNames() {
    return authorizationGroupsList.stream().map(AuthorizationGroups::getAttributeName).collect(Collectors.toList());
  }

  @JsonIgnore
  public void add(AuthorizationGroups authorizationGroups) {
    this.authorizationGroupsList.add(authorizationGroups);
  }
}
