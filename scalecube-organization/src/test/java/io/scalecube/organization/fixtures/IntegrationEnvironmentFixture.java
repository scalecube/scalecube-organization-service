package io.scalecube.organization.fixtures;

import io.scalecube.services.gateway.clientsdk.Client;
import io.scalecube.services.gateway.clientsdk.ClientSettings;
import io.scalecube.test.fixtures.Fixture;
import java.time.Duration;
import org.opentest4j.TestAbortedException;
import reactor.netty.resources.LoopResources;

public final class IntegrationEnvironmentFixture implements Fixture {

  private static final IntegrationEnvironment environment = new IntegrationEnvironment();

  static {
    environment.start();
  }

  private Client client;

  @Override
  public void setUp() throws TestAbortedException {
    ClientSettings settings =
        ClientSettings.builder()
            .host("localhost")
            .port(7070)
            .loopResources(LoopResources.create("integration-tests-client"))
            .build();

    client = Client.websocket(settings);
  }

  @Override
  public <T> T proxyFor(Class<? extends T> clazz) {
    return client.forService(clazz);
  }

  @Override
  public void tearDown() {
    if (client != null) {
      client.close().block(Duration.ofSeconds(10));
    }
  }
}
