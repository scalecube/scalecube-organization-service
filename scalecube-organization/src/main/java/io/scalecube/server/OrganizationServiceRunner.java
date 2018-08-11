package io.scalecube.server;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.couchbase.CouchbaseRepositoryFactory;
import io.scalecube.services.Microservices;
import io.scalecube.transport.Address;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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
    Properties settings = settings();
    Microservices.builder()
        .seeds(seeds(settings))
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

  private static Address[] seeds(Properties settings) throws Exception {
    try {
      return stringListValue(settings.getProperty(SEEDS))
          .stream().map(Address::from).toArray(Address[]::new);
    } catch (Throwable ex) {
      throw new Exception("Failed to parse seeds from settings", ex);
    }
  }

  private static Properties settings() throws Exception {
    try {
      Properties settings = new Properties();
      settings.load(OrganizationServiceRunner.class.getResourceAsStream("/settings.properties"));
      return settings;
    } catch (IOException ex) {
      throw new Exception("Failed to initialize", ex);
    }
  }


  private static List<String> stringListValue(String seeds) {
    if (seeds == null || seeds.length() == 0) {
      return DEFAULT_SEEDS;
    } else {
      return Arrays.asList(seeds.split(","));
    }
  }
}
