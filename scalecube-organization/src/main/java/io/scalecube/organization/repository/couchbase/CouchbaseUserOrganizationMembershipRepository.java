package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.organization.operation.Organization;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

final class CouchbaseUserOrganizationMembershipRepository
    extends CouchbaseEntityRepository<Organization, String>
    implements UserOrganizationMembershipRepository {

  CouchbaseUserOrganizationMembershipRepository(Bucket bucket) {
    super(bucket, Organization.class);
  }

  @Override
  public void addMember(Organization organization, OrganizationMember member) {
    findById(organization.id())
        .ifPresent(
            org -> {
              OrganizationMember[] members = Arrays.copyOf(org.members(), org.members().length + 1);
              members[org.members().length] = member;

              Organization updatedOrganization =
                  Organization.builder().members(members).apiKeys(org.apiKeys()).copy(org);

              save(updatedOrganization.id(), updatedOrganization);
            });
  }

  @Override
  public boolean isMember(String userId, Organization organization) {
    return Arrays.stream(organization.members()).anyMatch(member -> member.id().equals(userId));
  }

  @Override
  public Collection<OrganizationMember> getMembers(Organization organization) {
    return Arrays.asList(organization.members());
  }

  @Override
  public void removeMember(String userId, Organization organization) {
    findById(organization.id())
        .ifPresent(
            org -> {
              Organization updatedOrganization =
                  Organization.builder()
                      .members(
                          Stream.of(org.members())
                              .filter(member -> !member.id().equals(userId))
                              .toArray(OrganizationMember[]::new))
                      .apiKeys(org.apiKeys())
                      .copy(org);

              save(updatedOrganization.id(), updatedOrganization);
            });
  }

  @Override
  public Optional<OrganizationMember> getMember(String userId, Organization organization) {
    return Stream.of(organization.members())
        .filter(member -> member.id().equals(userId))
        .findFirst();
  }
}
