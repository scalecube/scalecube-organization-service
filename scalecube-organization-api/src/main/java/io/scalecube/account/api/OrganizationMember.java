package io.scalecube.account.api;

import java.util.Objects;

public class OrganizationMember {

  private User user;
  private String role;

  public OrganizationMember() {}

  public OrganizationMember(User user, String role) {
    this.user = user;
    this.role = role;
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
    return Objects.equals(user, other.user) && Objects.equals(role, other.role);
  }

  @Override
  public String toString() {
    return super.toString() + String.format("[user=%s, role=%s]", user, role);
  }
}
