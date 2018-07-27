package io.scalecube.server;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.User;
import io.scalecube.account.tokens.DefaultTokenVerification;
import io.scalecube.organization.OrganizationServiceImpl;
import io.scalecube.organization.repository.couchbase.CouchbaseRepositoryFactory;
import io.scalecube.services.Microservices;
import io.scalecube.transport.Address;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class OrganizationServiceRunner {
  private static final List<String> DEFAULT_SEEDS = Collections.singletonList("seed:4802");
  private static final String SEEDS = "seeds";

  /**
   * AccountBootstrap main.
   * 
   * @param args application params.
   */
  public static void main(String[] args) throws InterruptedException {
    start();

    Thread.currentThread().join();
  }

  private static void start() {
    Microservices.builder()
            .seeds(seeds())
            .services(new OrganizationServiceImpl
                  .Builder()
                  .organizationRepository(
                          CouchbaseRepositoryFactory.organizations())
                  .userRepository(
                          CouchbaseRepositoryFactory.users())
                  .organizationMembershipRepository(
                          CouchbaseRepositoryFactory.organizationMembers())
                  .organizationMembershipRepositoryAdmin(
                          CouchbaseRepositoryFactory
                                  .organizationMembersRepositoryAdmin()))
            .startAwait();
  }

  private static Address[] seeds() {
    try {
      Properties settings = new Properties();
      settings.load(OrganizationServiceRunner.class.getResourceAsStream("/settings.properties"));
      return stringListValue(settings.getProperty(SEEDS))
              .stream().map(Address::from).toArray(Address[]::new);
    } catch (IOException e) {
      throw new RuntimeException("Failed to initialize", e);
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
