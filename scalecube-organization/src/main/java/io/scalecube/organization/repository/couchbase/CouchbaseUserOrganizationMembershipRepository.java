package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

final class CouchbaseUserOrganizationMembershipRepository extends
    CouchbaseEntityRepository<OrganizationMember, String>
    implements UserOrganizationMembershipRepository {

  private final CouchbaseSettings settings = new CouchbaseSettings.Builder().build();

  CouchbaseUserOrganizationMembershipRepository() {
    super(null, OrganizationMember.class);
  }


  @Override
  public void addMember(Organization org, OrganizationMember member) {
    save(client(org), member.id(), member);
  }

  @Override
  public boolean isMember(String userId, Organization organization) {
    return existsById(client(organization), userId);
  }

  @Override
  public Collection<OrganizationMember> getMembers(Organization organization) {
    return StreamSupport
        .stream(findAll(client(organization)).spliterator(), false)
        .collect(Collectors.toList());
  }

  @Override
  public void removeMember(String userId, Organization organization) {
    deleteById(client(organization), userId);
  }

  @Override
  public Optional<OrganizationMember> getMember(String userId, Organization organization) {
    return findById(client(organization), userId);
  }

  private Bucket client(Organization organization) {
    return client(getBucketName(organization), organization.id());
  }

  private Bucket client(String bucketName, String bucketPassword) {
    return cluster().openBucket(bucketName, bucketPassword);
  }

  private String getBucketName(Organization organization) {
    return String.format(settings.getOrgMembersBucketSuffix(), organization.name());
  }
}
