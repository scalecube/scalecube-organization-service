package io.scalecube.organization.domain;

import static java.util.Objects.requireNonNull;

import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.Role;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

/** Represents an Organization. */
public class Organization extends Entity {

  private String name;
  private String email;
  private Set<OrganizationMember> members = new HashSet<>();
  private Set<ApiKey> apiKeys = new HashSet<>();

  Organization() {}

  /**
   * Creates new instance of organization.
   *
   * @param id organization id.
   * @param name organization name.
   * @param email organization email.
   * @param creatorUserId user id of organization creator.
   */
  public Organization(String id, String name, String email, String creatorUserId) {
    this.id = requireNonNull(id, "organization id cannot be null");
    this.name = requireNonNull(name, "organization name cannot be null");
    this.email = requireNonNull(email, "organization email cannot be null");

    addMember(
        new OrganizationMember(
            requireNonNull(creatorUserId, "organization creator id cannot be null"),
            Role.Owner.name()));
  }

  public String name() {
    return name;
  }

  public String email() {
    return email;
  }

  public Set<OrganizationMember> members() {
    return Collections.unmodifiableSet(members);
  }

  public Set<ApiKey> apiKeys() {
    return Collections.unmodifiableSet(apiKeys);
  }

  public void changeName(String name) {
    this.name = name;
  }

  public void changeEmail(String email) {
    this.email = email;
  }

  public void addMember(OrganizationMember member) {
    members.add(member);
  }

  public void removeMember(String userId) {
    members.removeIf(member -> member.id().equals(userId));
  }

  public boolean isMember(String userId) {
    return members.stream().anyMatch(member -> member.id().equals(userId));
  }

  public void updateMemberRole(String userId, Role role) {
    removeMember(userId);
    addMember(new OrganizationMember(userId, role.name()));
  }

  public void addApiKey(ApiKey apiKey) {
    apiKeys.add(apiKey);
  }

  public void removeApiKey(String apiKeyName) {
    apiKeys.removeIf(apiKey -> apiKey.name().equals(apiKeyName));
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Organization.class.getSimpleName() + "[", "]")
        .add("id='" + id + "'")
        .add("name='" + name + "'")
        .add("email='" + email + "'")
        .add("members=" + members)
        .toString();
  }
}
