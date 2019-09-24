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
import java.time.Duration;
import org.opentest4j.TestAbortedException;

public final class IntegrationEnvironmentFixture implements Fixture {

  private static final IntegrationEnvironment environment = new IntegrationEnvironment();

  private static final int GATEWAY_WS_PORT = 7070;
  private static final String GATEWAY_HOST = "localhost";

  static {
    environment.start();
  }

  private GatewayClient client;
  private ServiceCall serviceCall;

  @Override
  public void setUp() throws TestAbortedException {
    GatewayClientSettings settings =
        GatewayClientSettings.builder().host(GATEWAY_HOST).port(GATEWAY_WS_PORT).build();

    client = new WebsocketGatewayClient(settings, GatewayClientTransports.WEBSOCKET_CLIENT_CODEC);
    serviceCall =
        new ServiceCall()
            .transport(new GatewayClientTransport(client))
            .router(new StaticAddressRouter(Address.create(settings.host(), settings.port())));
  }

  @Override
  public <T> T proxyFor(Class<? extends T> clazz) {
    return serviceCall.api(clazz);
  }

  @Override
  public void tearDown() {
    if (client != null) {
      client.close();
      client.onClose().block(Duration.ofSeconds(10));
    }
  }
}
