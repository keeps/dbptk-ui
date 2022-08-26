package com.databasepreservation.common.client.models.authorization;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Gabriel Barros <gbarros@keep.pt>
 */
public class AuthorizationRuleList implements Serializable {
  private static final long serialVersionUID = -3730186735554294942L;

  private Set<AuthorizationRules> authorizationRulesList = new HashSet<>();

  public Set<AuthorizationRules> getAuthorizationRulesList() {
    return authorizationRulesList;
  }


  @JsonIgnore
  public AuthorizationRules get(String id) {
    for (AuthorizationRules authorizationRules : authorizationRulesList) {
      if (authorizationRules.getId().equals(id)) {
        return authorizationRules;
      }
    }
    return null;
  }

  @JsonIgnore
  public void add(AuthorizationRules authorizationRules) {
    this.authorizationRulesList.add(authorizationRules);
  }
}
