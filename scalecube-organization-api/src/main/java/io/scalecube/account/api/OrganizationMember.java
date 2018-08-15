package io.scalecube.account.api;

import java.util.Objects;

/**
 * Represents an organization member.
 */
public class OrganizationMember {
  private String id;

  private String role;

  /**
   * Constructs an empty organization member.
   */
  public OrganizationMember() {}

  /**
   * Constructs an organization member using the user and role arguments.
   * @param role organization member role.
   */
  public OrganizationMember(String userId, String role) {
    this.id = userId;
    this.role = role;
  }

  public String id() {
    return this.id;
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

    return Objects.equals(id, other.id);
  }

  @Override
  public String toString() {
    return super.toString() + String.format("[userId=%s, role=%s]", id, role);
  }
}
