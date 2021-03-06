package io.scalecube.account.api;

import java.util.Objects;
import java.util.StringJoiner;

/** Represents an organization member. */
public class OrganizationMember {

  private String id;
  private String role;

  /** Constructs an empty organization member. */
  OrganizationMember() {}

  /**
   * Constructs an organization member using the user and role arguments.
   *
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
    OrganizationMember other = (OrganizationMember) obj;
    return Objects.equals(id, other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", OrganizationMember.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("role='" + role + "'")
        .toString();
  }
}
