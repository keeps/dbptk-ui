/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/dbptk-ui
 */
package com.databasepreservation.common.server.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.roda.core.data.v2.user.RodaPrincipal;

import com.databasepreservation.common.client.ViewerConstants;
import com.databasepreservation.common.client.models.authorization.AuthorizationGroupsList;
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

      final String rolesConfigurationValue = ViewerConfiguration.getInstance().getViewerConfigurationAsString(
        ViewerConstants.DEFAULT_ATTRIBUTE_ROLES, ViewerConfiguration.PROPERTY_AUTHORIZATION_ROLES_ATTRIBUTE);
      final String fullNameConfigurationValue = ViewerConfiguration.getInstance().getViewerConfigurationAsString(
        ViewerConstants.DEFAULT_ATTRIBUTE_FULLNAME, ViewerConfiguration.PROPERTY_AUTHORIZATION_FULLNAME_ATTRIBUTE);
      final String emailConfigurationValue = ViewerConfiguration.getInstance().getViewerConfigurationAsString(
        ViewerConstants.DEFAULT_ATTRIBUTE_EMAIL, ViewerConfiguration.PROPERTY_AUTHORIZATION_EMAIL_ATTRIBUTE);

      Set<String> attributeRolesToCheck = new HashSet<>();
      attributeRolesToCheck.add(rolesConfigurationValue);

      AuthorizationGroupsList authorizationGroups = ViewerConfiguration.getInstance()
        .getCollectionsAuthorizationGroups();
      attributeRolesToCheck.addAll(authorizationGroups.getAllAttributeNames());

      for (String attributeRole : attributeRolesToCheck) {
        Set<String> roles = new HashSet<>();
        roles.addAll(user.getAllRoles());
        roles.addAll(user.getDirectRoles());
        if (attributes.get(attributeRole) instanceof String) {
          mapCasAttributeString(attributes, attributeRole, roles::addAll);
        } else if (attributes.get(attributeRole) instanceof List) {
          mapCasAttributeList(attributes, attributeRole, roles::addAll);
        }
        user.setAllRoles(roles);
        user.setDirectRoles(roles);
      }

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

  private static void mapCasAttributeList(Map<String, Object> attributes, String attributeKey,
    Consumer<Set<String>> mapping) {
    Object attributeValue = attributes.get(attributeKey);
    Set<String> result = new HashSet<>();
    if (attributeValue instanceof List) {
      for (int i = 0; i < ((List<?>) attributeValue).size(); i++) {
        Object item = ((List<?>) attributeValue).get(i);
        if (item instanceof String) {
          result.add((String) item);
        }
      }

      mapping.accept(result);
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
