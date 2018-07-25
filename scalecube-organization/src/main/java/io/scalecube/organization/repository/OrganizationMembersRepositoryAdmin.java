package io.scalecube.organization.repository;

import io.scalecube.account.api.Organization;

public interface OrganizationMembersRepositoryAdmin {
    void createRepository(Organization organization);

    void deleteRepository(Organization organization);
}
