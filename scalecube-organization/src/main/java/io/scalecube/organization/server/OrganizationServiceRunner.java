package io.scalecube.organization.server;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.config.AppConfiguration;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.repository.couchbase.CouchbaseOrganizationsRepository;
import io.scalecube.organization.repository.couchbase.CouchbaseSettings;
import io.scalecube.organization.tokens.Auth0PublicKeyProvider;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.organization.tokens.TokenVerifierImpl;
import io.scalecube.organization.tokens.store.KeyStore;
import io.scalecube.organization.tokens.store.VaultKeyStore;
import io.scalecube.services.Microservices;
import io.scalecube.services.discovery.ScalecubeServiceDiscovery;
import io.scalecube.services.transport.rsocket.RSocketServiceTransport;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/** Service runner main entry point. */
public class OrganizationServiceRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceRunner.class);

  /**
   * Bootstrap main.
   *
   * @param args application params.
   */
  public static void main(String[] args) throws Exception {
    start();
    Thread.currentThread().join();
  }

  private static void start() {
    DiscoveryOptions discoveryOptions =
        AppConfiguration.configRegistry()
            .objectProperty("io.scalecube.organization", DiscoveryOptions.class)
            .value()
            .orElseThrow(() -> new IllegalStateException("Couldn't load discovery options"));

    LOGGER.info("Starting organization service on {}", discoveryOptions);

    Microservices.builder()
        .discovery(
            (serviceEndpoint) ->
                new ScalecubeServiceDiscovery(serviceEndpoint)
                    .options(
                        opts ->
                            opts.seedMembers(discoveryOptions.seeds())
                                .port(discoveryOptions.discoveryPort())
                                .memberHost(discoveryOptions.memberHost())
                                .memberPort(discoveryOptions.memberPort())))
        .transport(
            opts ->
                opts.serviceTransport(RSocketServiceTransport::new)
                    .port(discoveryOptions.servicePort()))
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
    KeyStore keyStore = new VaultKeyStore();
    TokenVerifier tokenVerifier = new TokenVerifierImpl(new Auth0PublicKeyProvider());

    return new OrganizationServiceImpl(repository, keyStore, tokenVerifier);
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
