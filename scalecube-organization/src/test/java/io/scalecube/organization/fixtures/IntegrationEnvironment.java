package io.scalecube.organization.fixtures;

import static io.scalecube.organization.fixtures.EnvUtils.setEnv;
import static io.scalecube.organization.scenario.BaseScenario.API_KEY_TTL_IN_SECONDS;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.github.dockerjava.api.model.PortBinding;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.net.Address;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.config.AppConfiguration;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.repository.couchbase.CouchbaseOrganizationsRepository;
import io.scalecube.organization.repository.couchbase.CouchbaseSettings;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.organization.tokens.TokenVerifierImpl;
import io.scalecube.organization.tokens.store.VaultKeyStore;
import io.scalecube.services.Microservices;
import io.scalecube.services.discovery.ScalecubeServiceDiscovery;
import io.scalecube.services.gateway.ws.WebsocketGateway;
import io.scalecube.services.transport.rsocket.RSocketServiceTransport;
import io.scalecube.services.transport.rsocket.RSocketTransportResources;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.vault.VaultContainer;
import reactor.core.publisher.Mono;

final class IntegrationEnvironment {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationEnvironmentFixture.class);

  private static final String COUCHBASE_DOCKER_IMAGE = "couchbase:community-6.0.0";
  private static final String COUCHBASE_USERNAME = "admin";
  private static final String COUCHBASE_PASSWORD = "123456";
  private static final String BUCKET_FULL_ACCESS = "bucket_full_access";

  private static final String VAULT_DOCKER_IMAGE = "vault:0.9.5";
  private static final int VAULT_PORT = 8200;
  private static final String VAULT_TOKEN = "token_for_benchmarks";
  private static final String VAULT_SECRETS_PATH = "secret/configuration-service/dev";
  private static final String VAULT_ADDR_PATTERN = "http://%s:%d";

  private static final int GATEWAY_WS_PORT = 7070;
  private static final int GATEWAY_DISCOVERY_PORT = 4801;
  private static final int GATEWAY_TRANSPORT_PORT = 5801;

  private static final int ORG_SERVICE_DISCOVERY_PORT = 4802;
  private static final int ORG_SERVICE_TRANSPORT_PORT = 5802;

  private CouchbaseContainer couchbase;
  private VaultContainer vault;
  private Microservices gateway;
  private Microservices organizationService;

  void start() {
    LOGGER.info("### Start environment");

    try {
      couchbase = startCouchbase();
      vault = startVault();
      gateway = startGateway();
      organizationService = startOrganizationService();

    } catch (Exception e) {
      LOGGER.error("### Error on environment set up", e);

      stop();

      throw new RuntimeException("Error on environment set up", e);
    }

    LOGGER.info("### Environment is running");
  }

  void stop() {
    LOGGER.info("### Stop environment");

    try {
      if (organizationService != null) {
        organizationService.shutdown().block();
      }
      if (gateway != null) {
        gateway.shutdown().block();
      }
      if (vault != null) {
        vault.stop();
      }
      if (couchbase != null) {
        couchbase.stop();
      }

      TimeUnit.SECONDS.sleep(5);
    } catch (Exception e) {
      LOGGER.error("### Error on stopping environment", e);
      throw new RuntimeException("Error on stopping environment", e);
    }

    LOGGER.info("### Environment is stopped");
  }

  private CouchbaseContainer startCouchbase() {
    LOGGER.info("### Start couchbase");

    CouchbaseContainer couchbase =
        new CouchbaseContainer(COUCHBASE_DOCKER_IMAGE)
            .withClusterAdmin(COUCHBASE_USERNAME, COUCHBASE_PASSWORD)
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.withName("couchbase-" + RandomStringUtils.randomAlphabetic(5));
                  cmd.withPortBindings(PortBinding.parse("8091:8091"));
                });
    couchbase.start();
    couchbase.initCluster();
    try {
      couchbase.callCouchbaseRestAPI("/settings/indexes", "storageMode=forestdb");
    } catch (IOException e) {
      // ignore
    }

    String name = "organizations";
    couchbase.createBucket(
        DefaultBucketSettings.builder().name(name).password(COUCHBASE_PASSWORD).build(),
        UserSettings.build()
            .name(name)
            .password(COUCHBASE_PASSWORD)
            .roles(Collections.singletonList(new UserRole(BUCKET_FULL_ACCESS, name))),
        true);

    String configName = "configurations";
    couchbase.createBucket(
        DefaultBucketSettings.builder().name(configName).password(COUCHBASE_PASSWORD).build(),
        UserSettings.build()
            .name(configName)
            .password(COUCHBASE_PASSWORD)
            .roles(Collections.singletonList(new UserRole(BUCKET_FULL_ACCESS, configName))),
        true);

    couchbase.getCouchbaseCluster().disconnect();
    couchbase.getCouchbaseEnvironment().shutdown();

    return couchbase;
  }

  private VaultContainer startVault() {
    LOGGER.info("### Start vault");

    VaultContainer<?> vault =
        new VaultContainer<>(VAULT_DOCKER_IMAGE)
            .withVaultPort(VAULT_PORT)
            .withVaultToken(VAULT_TOKEN)
            .withSecretInVault(
                VAULT_SECRETS_PATH,
                "couchbase.hosts=localhost",
                "couchbase.username=" + COUCHBASE_USERNAME,
                "couchbase.password=" + COUCHBASE_PASSWORD,
                "organizations.bucket=organizations",
                "token.expiration=" + API_KEY_TTL_IN_SECONDS * 1000,
                "api.keys.path.pattern=%s/api-keys/",
                "key.cache.ttl=2",
                "key.cache.refresh.interval=1")
            .withCreateContainerCmdModifier(
                cmd -> cmd.withName("vault-" + RandomStringUtils.randomAlphabetic(5)))
            .waitingFor(new LogMessageWaitStrategy().withRegEx("^.*Vault server started!.*$"));
    vault.start();

    return vault;
  }

  private Microservices startGateway() {
    LOGGER.info("### Start gateway");

    return Microservices.builder()
        .discovery(
            serviceEndpoint ->
                new ScalecubeServiceDiscovery(serviceEndpoint)
                    .options(opts -> opts.port(GATEWAY_DISCOVERY_PORT)))
        .transport(
            opts ->
                opts.resources(RSocketTransportResources::new)
                    .client(RSocketServiceTransport.INSTANCE::clientTransport)
                    .server(RSocketServiceTransport.INSTANCE::serverTransport)
                    .port(GATEWAY_TRANSPORT_PORT))
        .gateway(options -> new WebsocketGateway(options.port(GATEWAY_WS_PORT)))
        .startAwait();
  }

  private Microservices startOrganizationService() {
    LOGGER.info("### Start organization service");

    setEnv("VAULT_ADDR", String.format(VAULT_ADDR_PATTERN, "localhost", VAULT_PORT));
    setEnv("VAULT_SECRETS_PATH", VAULT_SECRETS_PATH);
    setEnv("VAULT_TOKEN", VAULT_TOKEN);

    return Microservices.builder()
        .discovery(
            serviceEndpoint ->
                new ScalecubeServiceDiscovery(serviceEndpoint)
                    .options(
                        opts ->
                            opts.seedMembers(Address.create("localhost", GATEWAY_DISCOVERY_PORT))
                                .port(ORG_SERVICE_DISCOVERY_PORT)))
        .transport(
            opts ->
                opts.resources(RSocketTransportResources::new)
                    .client(RSocketServiceTransport.INSTANCE::clientTransport)
                    .server(RSocketServiceTransport.INSTANCE::serverTransport)
                    .port(ORG_SERVICE_TRANSPORT_PORT))
        .services(createOrganizationService())
        .startAwait();
  }

  private static OrganizationService createOrganizationService() {
    CouchbaseSettings settings =
        AppConfiguration.configRegistry()
            .objectProperty(couchbaseSettingsBindingMap(), CouchbaseSettings.class)
            .value()
            .orElseThrow(() -> new IllegalStateException("Couldn't load couchbase settings"));

    Cluster cluster = CouchbaseCluster.create(settings.hosts());

    AsyncBucket bucket =
        Mono.fromCallable(
                () ->
                    cluster
                        .authenticate(settings.username(), settings.password())
                        .openBucket(settings.organizationsBucketName())
                        .async())
            .retryBackoff(3, Duration.ofSeconds(1))
            .block(Duration.ofSeconds(30));

    OrganizationsRepository repository = new CouchbaseOrganizationsRepository(bucket);

    TokenVerifier tokenVerifier = new TokenVerifierImpl(new InMemoryPublicKeyProvider());

    return new OrganizationServiceImpl(repository, new VaultKeyStore(), tokenVerifier);
  }

  private static Map<String, String> couchbaseSettingsBindingMap() {
    Map<String, String> bindingMap = new HashMap<>();

    bindingMap.put("hosts", "couchbase.hosts");
    bindingMap.put("username", "couchbase.username");
    bindingMap.put("password", "couchbase.password");
    bindingMap.put("organizationsBucketName", "organizations.bucket");

    return bindingMap;
  }
}
