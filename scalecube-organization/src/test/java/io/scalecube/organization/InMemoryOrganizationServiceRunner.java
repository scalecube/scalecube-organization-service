package io.scalecube.organization;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.ApiKey;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.Role;
import io.scalecube.account.api.Token;
import io.scalecube.net.Address;
import io.scalecube.organization.fixtures.InMemoryKeyStore;
import io.scalecube.organization.fixtures.InMemoryOrganizationRepository;
import io.scalecube.organization.fixtures.InMemoryPublicKeyProvider;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.PublicKeyProvider;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.organization.tokens.TokenVerifierImpl;
import io.scalecube.organization.tokens.store.KeyStore;
import io.scalecube.security.api.Profile;
import io.scalecube.services.Microservices;
import io.scalecube.services.discovery.ScalecubeServiceDiscovery;
import io.scalecube.services.transport.rsocket.RSocketServiceTransport;
import java.util.Arrays;
import java.util.Collections;

/** Service runner main entry point. */
public class InMemoryOrganizationServiceRunner {

  /**
   * Bootstrap main.
   *
   * @param args application params.
   */
  public static void main(String[] args) {
    Microservices.builder()
        .discovery(
            serviceEndpoint ->
                new ScalecubeServiceDiscovery(serviceEndpoint)
                    .options(
                        opts ->
                            opts.membership(
                                cfg -> cfg.seedMembers(Address.from("localhost:4801")))))
        .transport(RSocketServiceTransport::new)
        .services(createOrganizationService())
        .startAwait()
        .onShutdown()
        .block();
  }

  private static OrganizationService createOrganizationService() {
    OrganizationsRepository repository = new InMemoryOrganizationRepository();
    KeyStore keyStore = new InMemoryKeyStore();
    PublicKeyProvider keyProvider = new InMemoryPublicKeyProvider();
    TokenVerifier tokenVerifier = new TokenVerifierImpl(keyProvider);

    Profile profile =
        Profile.builder()
            .userId("test_user")
            .email("test_user@scalecube.io")
            .emailVerified(true)
            .name("test_user")
            .familyName("fname")
            .givenName("lname")
            .build();

    Token token = InMemoryPublicKeyProvider.token(profile);

    System.err.println("generated ``token` for `test_user` = " + token.token());

    OrganizationService service = new OrganizationServiceImpl(repository, keyStore, tokenVerifier);

    String orgId =
        service
            .createOrganization(new CreateOrganizationRequest("test_org", profile.email(), token))
            .map(OrganizationInfo::id)
            .block();

    ApiKey[] apiKeys =
        service
            .addOrganizationApiKey(
                new AddOrganizationApiKeyRequest(
                    token,
                    orgId,
                    "test_api_key",
                    Collections.singletonMap("role", Role.Owner.name())))
            .map(OrganizationInfo::apiKeys)
            .block();

    System.err.println("generated `apiKeys`: " + Arrays.toString(apiKeys));

    return service;
  }
}
