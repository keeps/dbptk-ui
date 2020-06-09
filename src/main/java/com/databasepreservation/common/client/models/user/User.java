package com.databasepreservation.common.client.models.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.user.RodaPrincipal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Miguel Guimarães <mguimaraes@keep.pt>
 */
public class User extends RodaPrincipal {
  /**
   * Email address.
   */
  private String email;
  /**
   * Is a guest user?
   */
  private boolean guest;
  /**
   * IP address.
   */
  private String ipAddress = "";

  /**
   * Is an admin user?
   */
  private boolean admin;

  /**
   * Is a whiteList user?
   */
  private boolean whiteList;

  /**
   * Constructor.
   */
  public User() {
    this((String) null);
  }

  /**
   * Constructs a new user with the given name.
   *
   * @param name the name of the new user.
   */
  public User(final String name) {
    this(name, name, false);
  }

  public User(final User user) {
    this(user.getId(), user.getName(), user.getFullName(), user.isAdmin(),user.isWhiteList(), user.isActive(), user.getAllRoles(), user.getDirectRoles(),
        user.getEmail(), user.isGuest(), user.getIpAddress());
  }

  public User(final String id, final String name, final boolean guest) {
    this(id, name, null, guest);
  }

  public User(final String id, final String name, final String email, final boolean guest) {
    this(id, name, email, guest, "", new HashSet<>(), new HashSet<>());
  }

  public User(final String id, final String name, final String email, final boolean guest, final String ipAddress,
              final Set<String> allRoles, final Set<String> directRoles) {
    this(id, name, name, false, false, true, allRoles, directRoles, email, guest, ipAddress);
  }

  public User(final String id, final String name, final String fullName, final boolean admin, final boolean whiteList, final boolean active,
              final Set<String> allRoles, final Set<String> directRoles, final String email,
              final boolean guest, final String ipAddress) {
    super(id, name, fullName, active, allRoles, directRoles);
    this.admin = admin;
    this.whiteList=whiteList;
    this.email = email;
    this.guest = guest;
    this.ipAddress = ipAddress;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public boolean isGuest() {
    return guest;
  }

  public void setGuest(final boolean guest) {
    this.guest = guest;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public User setIpAddress(final String ipAddress) {
    this.ipAddress = ipAddress;
    return this;
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public boolean isWhiteList() {
    return whiteList;
  }

  public void setWhiteList(boolean whiteList) {
    this.whiteList = whiteList;
  }

  @Override
  public boolean isUser() {
    return true;
  }

  /**
   * 20161102 hsilva: a <code>@JsonIgnore</code> should be added to avoid
   * serializing
   */
  @JsonIgnore
  @Override
  public int getClassVersion() {
    return 1;
  }

  /**
   * Return the fields to create lite
   *
   * @return a {@link List} of {@link String} with the fields.
   */
  @Override
  public List<String> liteFields() {
    return Arrays.asList(RodaConstants.INDEX_UUID, RodaConstants.MEMBERS_NAME);
  }
}
