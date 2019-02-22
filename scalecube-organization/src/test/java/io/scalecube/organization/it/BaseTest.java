package io.scalecube.organization.it;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.OrganizationsDataAccessImpl;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationMembersRepositoryAdmin;
import io.scalecube.organization.repository.inmem.InMemoryOrganizationRepository;
import io.scalecube.organization.repository.inmem.InMemoryPublicKeyProvider;
import io.scalecube.organization.repository.inmem.InMemoryUserOrganizationMembershipRepository;
import io.scalecube.organization.token.store.PropertiesFileKeyStore;
import io.scalecube.tokens.TokenVerifier;
import io.scalecube.tokens.TokenVerifierImpl;
import io.scalecube.tokens.store.KeyStore;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import reactor.test.StepVerifier;

abstract class BaseTest {

  static final Duration TIMEOUT = Duration.ofSeconds(1);

  OrganizationService organizationService;

  @BeforeAll
  static void beforeAll() {
    StepVerifier.setDefaultTimeout(TIMEOUT);
  }

  @BeforeEach
  void beforeEach() {
    OrganizationsDataAccessImpl dataAccess =
        new OrganizationsDataAccessImpl(
            new InMemoryOrganizationRepository(),
            new InMemoryUserOrganizationMembershipRepository(),
            new InMemoryOrganizationMembersRepositoryAdmin());
    TokenVerifier tokenVerifier = new TokenVerifierImpl(new InMemoryPublicKeyProvider());
    KeyStore keyStore = new PropertiesFileKeyStore();

    organizationService = new OrganizationServiceImpl(dataAccess, keyStore, tokenVerifier);
  }
}
