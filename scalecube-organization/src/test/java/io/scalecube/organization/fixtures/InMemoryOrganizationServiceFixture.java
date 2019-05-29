package io.scalecube.organization.fixtures;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.tokens.TokenVerifierImpl;
import io.scalecube.organization.tokens.store.KeyStore;
import io.scalecube.test.fixtures.Fixture;
import org.opentest4j.TestAbortedException;

public class InMemoryOrganizationServiceFixture implements Fixture {

  private OrganizationService service;

  @Override
  public void setUp() throws TestAbortedException {
    InMemoryOrganizationRepository organizationRepository = new InMemoryOrganizationRepository();

    TokenVerifierImpl tokenVerifier = new TokenVerifierImpl(new InMemoryPublicKeyProvider());

    KeyStore keyStore = new InMemoryKeyStore();

    service = new OrganizationServiceImpl(organizationRepository, keyStore, tokenVerifier);
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
    // do nothing
  }
}
