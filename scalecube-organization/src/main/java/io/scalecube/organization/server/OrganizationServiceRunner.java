package io.scalecube.organization.server;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.CouchbaseCluster;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.app.decoration.Logo;
import io.scalecube.app.packages.PackageInfo;
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
import io.scalecube.services.ServiceEndpoint;
import io.scalecube.services.discovery.ScalecubeServiceDiscovery;
import io.scalecube.services.transport.rsocket.RSocketServiceTransport;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;

/** Service runner main entry point. */
public class OrganizationServiceRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceRunner.class);

  /**
   * Bootstrap main.
   *
   * @param args application params.
   */
  public static void main(String[] args) {
    DiscoveryOptions discoveryOptions =
        AppConfiguration.configRegistry()
            .objectProperty("io.scalecube.organization", DiscoveryOptions.class)
            .value()
            .orElseThrow(() -> new IllegalStateException("Couldn't load discovery options"));

    LOGGER.info("Starting organization service on {}", discoveryOptions);

    Microservices.builder()
        .discovery((serviceEndpoint) -> serviceDiscovery(discoveryOptions, serviceEndpoint))
        .transport(() -> serviceTransport(discoveryOptions))
        .services(createOrganizationService())
        .start()
        .doOnNext(
            microservices ->
                Logo.from(new PackageInfo())
                    .ip(microservices.discovery().address().host())
                    .port("" + microservices.discovery().address().port())
                    .draw())
        .block()
        .onShutdown()
        .block();
  }

  private static RSocketServiceTransport serviceTransport(DiscoveryOptions options) {
    return new RSocketServiceTransport()
        .tcpClient(resources -> TcpClient.newConnection().runOn(resources))
        .tcpServer(resources -> TcpServer.create().port(options.servicePort()).runOn(resources));
  }

  private static ScalecubeServiceDiscovery serviceDiscovery(
      DiscoveryOptions discoveryOptions, ServiceEndpoint serviceEndpoint) {
    return new ScalecubeServiceDiscovery(serviceEndpoint)
        .options(
            opts ->
                opts.membership(m -> m.seedMembers(discoveryOptions.seeds()))
                    .transport(t -> t.port(discoveryOptions.discoveryPort()))
                    .memberHost(discoveryOptions.memberHost())
                    .memberPort(discoveryOptions.memberPort()));
  }

  private static OrganizationService createOrganizationService() {
    CouchbaseSettings settings =
        AppConfiguration.configRegistry()
            .objectProperty(couchbaseSettingsBindingMap(), CouchbaseSettings.class)
            .value()
            .orElseThrow(() -> new IllegalStateException("Couldn't load couchbase settings"));

    CouchbaseCluster couchbaseCluster = CouchbaseCluster.create(settings.hosts());

    AsyncBucket bucket =
        Mono.fromCallable(() -> newAsyncBucket(settings, couchbaseCluster))
            .retryBackoff(3, Duration.ofSeconds(1))
            .block(Duration.ofSeconds(30));

    OrganizationsRepository repository = new CouchbaseOrganizationsRepository(bucket);
    KeyStore keyStore = new VaultKeyStore();
    TokenVerifier tokenVerifier = new TokenVerifierImpl(new Auth0PublicKeyProvider());

    return new OrganizationServiceImpl(repository, keyStore, tokenVerifier);
  }

  private static AsyncBucket newAsyncBucket(
      CouchbaseSettings settings, CouchbaseCluster couchbaseCluster) {
    return couchbaseCluster
        .authenticate(settings.username(), settings.password())
        .openBucket(settings.organizationsBucketName())
        .async();
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
