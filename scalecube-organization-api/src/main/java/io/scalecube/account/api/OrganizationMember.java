package io.scalecube.account.api;

import java.util.Objects;

public class OrganizationMember {
  private final String id;

  private final User user;

  private final String role;

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
    if (!(obj instanceof OrganizationMember))
      return super.equals(obj);

    OrganizationMember other = (OrganizationMember)obj;
    return Objects.equals(user, other.id);
  }

  @Override
  public String toString() {
    return super.toString() + String.format("[user=%s, role=%s]", user, role);
  }
}
