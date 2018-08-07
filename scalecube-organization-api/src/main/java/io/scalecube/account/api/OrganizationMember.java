package io.scalecube.account.api;

import java.util.Objects;

/**
 * Represents an organization member.
 */
public class OrganizationMember {
  private String id;

  private User user;

  private String role;

  /**
   * Constructs an empty organization member.
   */
  public OrganizationMember() {}

  /**
   * Constructs an organization member using the user and role arguments.
   * @param user organization member user.
   * @param role organization member role.
   */
  public OrganizationMember(User user, String role) {
    this.id = user.id();
    this.user = user;
    this.role = role;
  }

  public String id() {
    return this.id;
  }

  public User user() {
    return this.user;
  }

  public String role() {
    return this.role;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof OrganizationMember)) {
      return super.equals(obj);
    }

    OrganizationMember other = (OrganizationMember)obj;

    return Objects.equals(user, other.id);
  }

  @Override
  public String toString() {
    return super.toString() + String.format("[user=%s, role=%s]", user, role);
  }
}
