package io.scalecube.server;

import io.scalecube.account.api.OrganizationService;
import io.scalecube.gateway.websocket.WebsocketServer;
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
  private static final String WEBSOCKET_PORT_KEY = "websocket.port";
  private static final String WEBSOCKET_PORT_DEFAULT_VALUE = "8080";
  public static final int DEFAULT_WEBSOCKET_PORT = 8080;

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
    Properties settings  = settings();
    Microservices microservices = Microservices.builder()
            //.seeds(seeds())
            .services(createOrganizationService())
            .startAwait();
    WebsocketServer server = new WebsocketServer(microservices);
    server.start(tryGetWebSocketPort(settings));
  }

  private static int tryGetWebSocketPort(Properties settings) {
    int port;

    try {
      port = Integer.parseInt(
              settings.getProperty(WEBSOCKET_PORT_KEY,
                    String.valueOf(DEFAULT_WEBSOCKET_PORT)));
    } catch (NumberFormatException e) {
      return DEFAULT_WEBSOCKET_PORT;
    }

    return port;
  }

  private static OrganizationService createOrganizationService() {
    return new OrganizationServiceImpl
            .Builder()
            .organizationRepository(CouchbaseRepositoryFactory.organizations())
            .userRepository(CouchbaseRepositoryFactory.users())
            .organizationMembershipRepository(CouchbaseRepositoryFactory.organizationMembers())
            .organizationMembershipRepositoryAdmin(CouchbaseRepositoryFactory.organizationMembersRepositoryAdmin())
            .build();
  }

  private static Address[] seeds(Properties settings) {
    try {
      return stringListValue(settings.getProperty(SEEDS))
              .stream().map(Address::from).toArray(Address[]::new);
    } catch (Throwable e) {
      throw new RuntimeException("Failed to parse seeds from settings", e);
    }
  }

  private static Properties settings() {
    try {
      Properties settings = new Properties();
      settings.load(OrganizationServiceRunner.class.getResourceAsStream("/settings.properties"));
      return settings;
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
