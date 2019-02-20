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
import io.scalecube.organization.token.store.PropertiesFileKeyStore;
import io.scalecube.test.fixtures.Fixture;
import io.scalecube.tokens.TokenVerifierImpl;
import io.scalecube.tokens.store.KeyStore;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.opentest4j.TestAbortedException;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.vault.VaultContainer;

public class DockerOrganizationServiceFixture implements Fixture {

  private static final String VAULT_DOCKER_IMAGE = "vault:0.9.5";
  private static final int VAULT_PORT = 8200;
  private static final String VAULT_TOKEN = "token_for_benchmarks";
  private static final String VAULT_SECRETS_PATH = "secret/configuration-service/dev";
  private static final String VAULT_NETWORK_ALIAS = "vault";
  private static final String VAULT_ADDR_PATTERN = "http://%s:%d";
  private static final WaitStrategy VAULT_SERVER_STARTED =
      new LogMessageWaitStrategy().withRegEx("^.*Vault server started!.*$");

  Map<String, String> env = new HashMap<>();



  private OrganizationService service;

  @Override
  public void setUp() throws TestAbortedException {


    env.put("VAULT_ADDR", String.format(VAULT_ADDR_PATTERN, VAULT_NETWORK_ALIAS, VAULT_PORT));
    env.put("VAULT_SECRETS_PATH", VAULT_SECRETS_PATH);
    env.put("VAULT_TOKEN", VAULT_TOKEN);


    VaultContainer<?> vault =
        new VaultContainer<>(VAULT_DOCKER_IMAGE)
            .withVaultPort(VAULT_PORT)
            .withVaultToken(VAULT_TOKEN)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(VAULT_NETWORK_ALIAS)
            .withCreateContainerCmdModifier(cmd -> cmd.withName(VAULT_NETWORK_ALIAS))
            .waitingFor(VAULT_SERVER_STARTED);
    vault.start();


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
