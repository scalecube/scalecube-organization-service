package io.scalecube.organization.fixtures;

import io.scalecube.services.gateway.clientsdk.Client;
import io.scalecube.services.gateway.clientsdk.ClientSettings;
import io.scalecube.test.fixtures.Fixture;
import org.opentest4j.TestAbortedException;
import reactor.netty.resources.LoopResources;

public final class IntegrationEnvironmentFixture implements Fixture {

  private static final IntegrationEnvironment environment = new IntegrationEnvironment();

  private static Client client;

  static {
    environment.start();

    ClientSettings settings =
        ClientSettings.builder()
            .host("localhost")
            .port(7070)
            .loopResources(LoopResources.create("integration-tests-client"))
            .build();

    client = Client.websocket(settings);
  }

  @Override
  public void setUp() throws TestAbortedException {
    // do nothing
  }

  @Override
  public <T> T proxyFor(Class<? extends T> clazz) {
    return client.forService(clazz);
  }

  @Override
  public void tearDown() {
    // do nothing
  }
}
