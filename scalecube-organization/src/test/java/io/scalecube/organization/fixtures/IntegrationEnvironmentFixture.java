package io.scalecube.organization.fixtures;

import io.scalecube.net.Address;
import io.scalecube.services.ServiceCall;
import io.scalecube.services.gateway.transport.GatewayClient;
import io.scalecube.services.gateway.transport.GatewayClientSettings;
import io.scalecube.services.gateway.transport.GatewayClientTransport;
import io.scalecube.services.gateway.transport.GatewayClientTransports;
import io.scalecube.services.gateway.transport.StaticAddressRouter;
import io.scalecube.services.gateway.transport.websocket.WebsocketGatewayClient;
import io.scalecube.test.fixtures.Fixture;
import org.opentest4j.TestAbortedException;

public final class IntegrationEnvironmentFixture implements Fixture {

  private static final IntegrationEnvironment environment = new IntegrationEnvironment();

  static {
    environment.start();
  }

  private GatewayClient client;
  private ServiceCall serviceCall;

  @Override
  public void setUp() throws TestAbortedException {
    GatewayClientSettings settings =
        GatewayClientSettings.builder().address(Address.create("localhost", 7070)).build();

    client = new WebsocketGatewayClient(settings, GatewayClientTransports.WEBSOCKET_CLIENT_CODEC);
    serviceCall =
        new ServiceCall()
            .transport(new GatewayClientTransport(client))
            .router(new StaticAddressRouter(Address.create(settings.host(), settings.port())));
  }

  @Override
  public <T> T proxyFor(Class<? extends T> clasz) {
    return serviceCall.api(clasz);
  }

  @Override
  public void tearDown() {
    if (client != null) {
      client.close();
      client.onClose().block();
    }
  }
}
