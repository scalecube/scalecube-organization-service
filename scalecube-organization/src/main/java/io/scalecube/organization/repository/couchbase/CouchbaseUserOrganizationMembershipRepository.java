package io.scalecube.organization.repository.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import io.scalecube.account.api.Organization;
import io.scalecube.account.api.OrganizationMember;
import io.scalecube.account.api.User;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

class CouchbaseUserOrganizationMembershipRepository extends CouchbaseEntityRepository<OrganizationMember, String>
        implements UserOrganizationMembershipRepository {

    private final CouchbaseSettings settings = new CouchbaseSettings.Builder().build();

    public CouchbaseUserOrganizationMembershipRepository() {
        super(null, OrganizationMember.class);
    }


    @Override
    public void addMember(Organization org, OrganizationMember member) {
        setCredentials(org);
        save(member.user().id(), member);
    }

    @Override
    public boolean isMember(User user, Organization organization) {
        setCredentials(organization);
        return existsById(user.id());
    }

    @Override
    public Collection<OrganizationMember> getMembers(Organization organization) {
        setCredentials(organization);
        return StreamSupport.stream(findAll().spliterator(), false).collect(Collectors.toList());
    }

    @Override
    public void removeMember(User user, Organization organization) {
        setCredentials(organization);
        deleteById(user.id());
    }

    @Override
    public Optional<OrganizationMember> getMember(User user, Organization organization) {
        setCredentials(organization);
        return findById(user.id());
    }

    private void setCredentials(Organization org) {
        this.bucketName = getBucketName(org);
        this.bucketPassword = org.id();
    }

    private String getBucketName(Organization organization) {
        return String.format(settings.getOrgMembersBucketSuffix(), organization.name());
    }
}
