package io.scalecube.server;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistryConfiguration;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.couchbase.CouchbaseRepositoryFactory;
import io.scalecube.services.Microservices;
import io.scalecube.services.discovery.api.DiscoveryConfig;
import io.scalecube.transport.Address;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    DiscoveryOptions discoveryOptions = discoveryOptions();
    LOGGER.info("Starting organization service on {}", discoveryOptions);

    Microservices.builder()
        .discoveryConfig(
            DiscoveryConfig.builder()
                .seeds(discoveryOptions.seeds())
                .port(discoveryOptions.discoveryPort())
                .memberHost(discoveryOptions.memberHost())
                .memberPort(discoveryOptions.memberPort()))
        .servicePort(discoveryOptions.servicePort())
        .services(createOrganizationService())
        .startAwait();
  }

  private static OrganizationService createOrganizationService() {
    return new OrganizationServiceImpl.Builder()
        .organizationRepository(CouchbaseRepositoryFactory.organizations())
        .organizationMembershipRepository(CouchbaseRepositoryFactory.organizationMembers())
        .organizationMembershipRepositoryAdmin(
            CouchbaseRepositoryFactory.organizationMembersRepositoryAdmin())
        .build();
  }

  private static DiscoveryOptions discoveryOptions() {
    ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();
    return configRegistry
        .objectProperty("io.scalecube.organization", DiscoveryOptions.class)
        .value()
        .orElseThrow(() -> new IllegalStateException("Couldn't load discovery options"));
  }

  public static class DiscoveryOptions {

    private List<String> seeds;
    private Integer servicePort;
    private Integer discoveryPort;
    private String memberHost;
    private Integer memberPort;

    public int servicePort() {
      return servicePort != null ? servicePort : 0;
    }

    public Integer discoveryPort() {
      return discoveryPort;
    }

    /**
     * Returns seeds as an {@link Address}'s array.
     *
     * @return {@link Address}'s array
     */
    public Address[] seeds() {
      return Optional.ofNullable(seeds)
          .map(seeds -> seeds.stream().map(Address::from).toArray(Address[]::new))
          .orElse(new Address[0]);
    }

    public String memberHost() {
      return memberHost;
    }

    public Integer memberPort() {
      return memberPort;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("DiscoveryOptions{");
      sb.append("seeds=").append(seeds);
      sb.append(", servicePort=").append(servicePort);
      sb.append(", discoveryPort=").append(discoveryPort);
      sb.append(", memberHost=").append(memberHost);
      sb.append(", memberPort=").append(memberPort);
      sb.append('}');
      return sb.toString();
    }
  }
}
