package io.scalecube.organization.fixtures;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.organization.MockPublicKeyProvider;
import io.scalecube.organization.Organization;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.OrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.OrganizationsDataAccessImpl;
import io.scalecube.organization.repository.Repository;
import io.scalecube.organization.repository.UserOrganizationMembershipRepository;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationRepository;
import io.scalecube.organization.repository.inmem.InMemoryUserOrganizationMembershipRepository;
import io.scalecube.test.fixtures.Fixture;
import io.scalecube.tokens.TokenVerifierImpl;
import java.io.File;
import org.opentest4j.TestAbortedException;

public class InMemoryOrganizationServiceFixture implements Fixture {

  private OrganizationService service;

  @Override
  public void setUp() throws TestAbortedException {
    UserOrganizationMembershipRepository orgMembersRepository =
        new InMemoryUserOrganizationMembershipRepository();
    Repository<Organization, String> organizationRepository = new InMemoryOrganizationRepository();
    OrganizationMembersRepositoryAdmin admin = new InMemoryOrganizationMembersRepositoryAdmin();

    OrganizationsDataAccess repository =
        OrganizationsDataAccessImpl.builder()
            .organizations(organizationRepository)
            .members(orgMembersRepository)
            .repositoryAdmin(admin)
            .build();
    TokenVerifierImpl tokenVerifier = new TokenVerifierImpl(new MockPublicKeyProvider());
    service = new OrganizationServiceImpl(repository, tokenVerifier);
  }

  @Override
  public <T> T proxyFor(Class<? extends T> clazz) {
    if (clazz.isAssignableFrom(OrganizationService.class)) {
      return clazz.cast(service);
    }
    return null;
  }

  @Override
  public void tearDown() {
    new File("keystore.properties").deleteOnExit();
  }
}
