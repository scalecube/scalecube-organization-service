package io.scalecube.server;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistryConfiguration;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.couchbase.CouchbaseRepositoryFactory;
import io.scalecube.services.Microservices;
import io.scalecube.services.discovery.api.DiscoveryConfig;
import io.scalecube.transport.Address;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service runner main entry point.
 */
public class OrganizationServiceRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceRunner.class);

  private static final List<String> DEFAULT_SEEDS = Collections.singletonList("seed:4802");
  private static final String SEEDS = "seeds";
  private static final String CLUSTER_MEMBER_DNS_NAME = "io.scalecube.cluster.member.dns.name";

  /**
   * Bootstrap main.
   *
   * @param args application params.
   */
  public static void main(String[] args) throws Exception {
    start();
    Thread.currentThread().join();
  }

  private static void start() throws Exception {

    String memberHost = memberHost();
    Address[] seeds = seeds();
    LOGGER.info("seeds={}, memberHost={}", Arrays.toString(seeds), memberHost);

    Microservices.builder()
        .discoveryConfig(DiscoveryConfig.builder().seeds(seeds).memberHost(memberHost))
        .services(createOrganizationService())
        .startAwait();
  }

  private static OrganizationService createOrganizationService() {
    return new OrganizationServiceImpl
        .Builder()
        .organizationRepository(CouchbaseRepositoryFactory.organizations())
        .organizationMembershipRepository(CouchbaseRepositoryFactory.organizationMembers())
        .organizationMembershipRepositoryAdmin(
            CouchbaseRepositoryFactory.organizationMembersRepositoryAdmin())
        .build();
  }

  private static Address[] seeds() throws Exception {
    ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();
    try {
      return configRegistry.stringListValue(SEEDS, DEFAULT_SEEDS)
          .stream().map(Address::from).toArray(Address[]::new);
    } catch (Throwable ex) {
      throw new Exception("Failed to parse seeds from settings", ex);
    }
  }

  private static String memberHost() {
    ConfigRegistry configRegistry = ConfigRegistryConfiguration.configRegistry();
    return configRegistry.stringValue(CLUSTER_MEMBER_DNS_NAME, null);
  }
}
