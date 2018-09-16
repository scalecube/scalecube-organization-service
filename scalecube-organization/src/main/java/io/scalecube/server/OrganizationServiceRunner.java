package io.scalecube.server;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.config.AppConfiguration;
import io.scalecube.config.ConfigRegistry;
import io.scalecube.config.ConfigRegistryConfiguration;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.couchbase.CouchbaseRepositoryFactory;
import io.scalecube.services.Microservices;
import io.scalecube.transport.Address;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Service runner main entry point.
 */
public class OrganizationServiceRunner {

  private static final List<String> DEFAULT_SEEDS = Collections.singletonList("seed:4802");
  private static final String SEEDS = "seeds";

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
    Microservices.builder()
        .seeds(seeds())
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
}
