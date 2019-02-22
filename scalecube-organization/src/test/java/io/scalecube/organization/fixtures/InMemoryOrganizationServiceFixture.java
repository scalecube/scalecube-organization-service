package io.scalecube.organization.fixtures;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.OrganizationsDataAccessImpl;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationRepository;
import io.scalecube.organization.repository.inmem.InMemoryPublicKeyProvider;
import io.scalecube.organization.repository.inmem.InMemoryUserOrganizationMembershipRepository;
import io.scalecube.organization.token.store.PropertiesFileKeyStore;
import io.scalecube.test.fixtures.Fixture;
import io.scalecube.tokens.TokenVerifierImpl;
import io.scalecube.tokens.store.KeyStore;
import java.io.File;
import org.opentest4j.TestAbortedException;

public class InMemoryOrganizationServiceFixture implements Fixture {

  private OrganizationService service;

  @Override
  public void setUp() throws TestAbortedException {
    OrganizationsDataAccess repository =
        new OrganizationsDataAccessImpl(
            new InMemoryOrganizationRepository(),
            new InMemoryUserOrganizationMembershipRepository(),
            new InMemoryOrganizationMembersRepositoryAdmin());

    TokenVerifierImpl tokenVerifier = new TokenVerifierImpl(new InMemoryPublicKeyProvider());

    KeyStore keyStore = new PropertiesFileKeyStore();

    service = new OrganizationServiceImpl(repository, keyStore, tokenVerifier);
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
