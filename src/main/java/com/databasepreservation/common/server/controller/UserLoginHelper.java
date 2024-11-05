/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import jakarta.servlet.http.HttpServletRequest;

import org.apereo.cas.client.authentication.AttributePrincipal;
import org.roda.core.data.v2.user.RodaPrincipal;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.authorization.AuthorizationGroup;
import com.databasepreservation.common.client.models.user.User;
import com.databasepreservation.common.server.ViewerConfiguration;
import com.databasepreservation.common.utils.UserUtility;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class UserLoginHelper {

  public static User casLogin(final String username, final HttpServletRequest request) {
    User user = new User(username);

    if (request.getUserPrincipal() instanceof AttributePrincipal) {
      AttributePrincipal attributePrincipal = (AttributePrincipal) request.getUserPrincipal();
      Map<String, Object> attributes = attributePrincipal.getAttributes();

      final List<String> rolesConfigurationValue = ViewerConfiguration.getInstance()
        .getViewerConfigurationAsList(ViewerConfiguration.PROPERTY_AUTHORIZATION_ROLES_ATTRIBUTE);
      final String fullNameConfigurationValue = ViewerConfiguration.getInstance().getViewerConfigurationAsString(
        ViewerConstants.DEFAULT_ATTRIBUTE_FULLNAME, ViewerConfiguration.PROPERTY_AUTHORIZATION_FULLNAME_ATTRIBUTE);
      final String emailConfigurationValue = ViewerConfiguration.getInstance().getViewerConfigurationAsString(
        ViewerConstants.DEFAULT_ATTRIBUTE_EMAIL, ViewerConfiguration.PROPERTY_AUTHORIZATION_EMAIL_ATTRIBUTE);

      user.setAllRoles(new HashSet<>());
      user.setDirectRoles(new HashSet<>());
      for (String rolesAttribute : rolesConfigurationValue) {
        if (attributes.get(rolesAttribute) instanceof String) {
          Set<String> roles = new HashSet<>();
          mapCasAttributeString(attributes, rolesAttribute, roles::addAll);
          user.getAllRoles().addAll(roles);
          user.getDirectRoles().addAll(roles);
        } else if (attributes.get(rolesAttribute) instanceof List) {
          mapCasAttributeList(user, attributes, rolesAttribute,
            (user1, allRoles) -> user1.getAllRoles().addAll(allRoles));
          mapCasAttributeList(user, attributes, rolesAttribute,
            (user1, directRoles) -> user1.getDirectRoles().addAll(directRoles));
        }
      }

      mapAuthorizedGroups(user, ViewerConfiguration.getInstance()
        .getCollectionsAuthorizationGroupsWithAdminAndUserRoles().getAuthorizationGroupsList());

      // Add default roles to authenticated user
      boolean addDefaultRoles = ViewerConfiguration.getInstance().getViewerConfigurationAsBoolean(false,
        ViewerConfiguration.PROPERTY_AUTHENTICATED_USER_ENABLE_DEFAULT_ATTRIBUTES);
      if (addDefaultRoles) {
        List<String> defaultRoleList = ViewerConfiguration.getInstance()
          .getViewerConfigurationAsList(ViewerConfiguration.PROPERTY_AUTHENTICATED_USER_DEFAULT_ATTRIBUTES);

        if (!defaultRoleList.isEmpty()) {
          Set<String> rolesWithDefault = new HashSet<>(user.getAllRoles());
          rolesWithDefault.addAll(defaultRoleList);
          user.setAllRoles(rolesWithDefault);
          user.setDirectRoles(rolesWithDefault);
        }
      }

      mapCasAttributeString(user, attributes, fullNameConfigurationValue, RodaPrincipal::setFullName);
      mapCasAttributeString(user, attributes, emailConfigurationValue, User::setEmail);
    }
    user.setAdmin(UserUtility.userIsAdmin(user));

    UserUtility.setUser(request, user);
    return user;
  }

  private static void mapAuthorizedGroups(User user, Set<AuthorizationGroup> authorizationGroups) {
    Set<String> authorizedRoles = new HashSet<>();
    for (AuthorizationGroup group : authorizationGroups) {
      List<String> rolesAttribute = ViewerConfiguration.getInstance()
        .getViewerConfigurationAsList(ViewerConfiguration.PROPERTY_AUTHORIZATION_ROLES_ATTRIBUTE);

      if (ViewerConfiguration.PROPERTY_COLLECTIONS_AUTHORIZATION_GROUP_OPERATOR_EQUAL
        .equals(group.getAttributeOperator())) {
        for (String roleAttribute : rolesAttribute) {
          if (roleAttribute.equalsIgnoreCase(group.getAttributeName())) {
            if (user.getAllRoles().stream().anyMatch(p -> p.equals(group.getAttributeValue()))) {
              authorizedRoles.add(group.getAttributeValue());
            }
          }
        }
      }
    }

    user.setAllRoles(authorizedRoles);
    user.setDirectRoles(authorizedRoles);
  }

  private static void mapCasAttributeList(User user, Map<String, Object> attributes, String attributeKey,
    BiConsumer<User, Set<String>> mapping) {
    Object attributeValue = attributes.get(attributeKey);
    Set<String> result = new HashSet<>();
    if (attributeValue instanceof List) {
      for (int i = 0; i < ((List<?>) attributeValue).size(); i++) {
        Object item = ((List<?>) attributeValue).get(i);
        if (item instanceof String) {
          result.add((String) item);
        }
      }

      mapping.accept(user, result);
    }
  }

  private static void mapCasAttributeString(User user, Map<String, Object> attributes, String attributeKey,
    BiConsumer<User, String> mapping) {
    Object attributeValue = attributes.get(attributeKey);
    if (attributeValue instanceof String) {
      mapping.accept(user, (String) attributeValue);
    }
  }

  private static void mapCasAttributeString(Map<String, Object> attributes, String attributeKey,
    Consumer<Set<String>> mapping) {
    Object attributeValue = attributes.get(attributeKey);
    if (attributeValue instanceof String) {
      Set<String> result = new HashSet<>();
      result.add((String) attributeValue);
      mapping.accept(result);
    }
  }
}
