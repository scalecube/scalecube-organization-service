package io.scalecube.organization.fixtures;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.OrganizationsDataAccess;
import io.scalecube.organization.repository.OrganizationsDataAccessImpl;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationRepository;
import io.scalecube.organization.repository.inmem.InMemoryPublicKeyProvider;
import io.scalecube.organization.token.store.PropertiesFileKeyStore;
import io.scalecube.organization.tokens.TokenVerifierImpl;
import io.scalecube.organization.tokens.store.KeyStore;
import io.scalecube.test.fixtures.Fixture;
import java.io.File;
import org.opentest4j.TestAbortedException;

public class InMemoryOrganizationServiceFixture implements Fixture {

  private OrganizationService service;

  @Override
  public void setUp() throws TestAbortedException {
    InMemoryOrganizationRepository organizationRepository = new InMemoryOrganizationRepository();

    OrganizationsDataAccess repository =
        new OrganizationsDataAccessImpl(organizationRepository, organizationRepository);

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
