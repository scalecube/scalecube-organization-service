package io.scalecube.organization.repository.inmem;

import io.scalecube.organization.operation.Organization;
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import java.util.HashSet;
import java.util.Set;

public class InMemoryOrganizationMembersRepositoryAdmin
    implements OrganizationMembersRepositoryAdmin {
  private final Set<Organization> set = new HashSet<>();

  @Override
  public void createRepository(Organization organization) {
    set.add(organization);
  }

  @Override
  public void deleteRepository(Organization organization) {
    set.remove(organization);
  }
}
