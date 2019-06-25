package io.scalecube.organization;

import io.scalecube.account.api.AddOrganizationApiKeyRequest;
import io.scalecube.account.api.CreateOrganizationRequest;
import io.scalecube.account.api.CreateOrganizationResponse;
import io.scalecube.account.api.DeleteOrganizationApiKeyRequest;
import io.scalecube.account.api.DeleteOrganizationRequest;
import io.scalecube.account.api.DeleteOrganizationResponse;
import io.scalecube.account.api.GetMembershipRequest;
import io.scalecube.account.api.GetMembershipResponse;
import io.scalecube.account.api.GetOrganizationMembersRequest;
import io.scalecube.account.api.GetOrganizationMembersResponse;
import io.scalecube.account.api.GetOrganizationRequest;
import io.scalecube.account.api.GetOrganizationResponse;
import io.scalecube.account.api.GetPublicKeyRequest;
import io.scalecube.account.api.GetPublicKeyResponse;
import io.scalecube.account.api.InviteOrganizationMemberRequest;
import io.scalecube.account.api.InviteOrganizationMemberResponse;
import io.scalecube.account.api.KickoutOrganizationMemberRequest;
import io.scalecube.account.api.KickoutOrganizationMemberResponse;
import io.scalecube.account.api.LeaveOrganizationRequest;
import io.scalecube.account.api.LeaveOrganizationResponse;
import io.scalecube.account.api.OrganizationService;
import io.scalecube.account.api.OrganizationServiceException;
import io.scalecube.account.api.ServiceOperationException;
import io.scalecube.account.api.UpdateOrganizationMemberRoleRequest;
import io.scalecube.account.api.UpdateOrganizationMemberRoleResponse;
import io.scalecube.account.api.UpdateOrganizationRequest;
import io.scalecube.account.api.UpdateOrganizationResponse;
import io.scalecube.organization.config.AppConfiguration;
import io.scalecube.organization.operation.AddOrganizationApiKey;
import io.scalecube.organization.operation.CreateOrganization;
import io.scalecube.organization.operation.DeleteOrganization;
import io.scalecube.organization.operation.DeleteOrganizationApiKey;
import io.scalecube.organization.operation.GetOrganization;
import io.scalecube.organization.operation.GetOrganizationMembers;
import io.scalecube.organization.operation.GetUserOrganizationsMembership;
import io.scalecube.organization.operation.InviteMember;
import io.scalecube.organization.operation.KickoutMember;
import io.scalecube.organization.operation.LeaveOrganization;
import io.scalecube.organization.operation.UpdateOrganization;
import io.scalecube.organization.operation.UpdateOrganizationMemberRole;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.tokens.TokenVerifier;
import io.scalecube.organization.tokens.store.KeyStore;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class OrganizationServiceImpl implements OrganizationService {

  private static final Logger logger = LoggerFactory.getLogger(OrganizationServiceImpl.class);

  private final TokenVerifier tokenVerifier;
  private final OrganizationsRepository repository;
  private final KeyStore keyStore;
  private final KeyPairGenerator keyPairGenerator;

  /**
   * Create instance of organization service.
   *
   * @param repository data access repository
   * @param keyStore key store
   * @param tokenVerifier token verifier
   */
  public OrganizationServiceImpl(
      OrganizationsRepository repository, KeyStore keyStore, TokenVerifier tokenVerifier) {
    this.repository = repository;
    this.keyStore = keyStore;
    this.tokenVerifier = tokenVerifier;
    this.keyPairGenerator = keyPairGenerator();
  }

  @Override
  public Mono<CreateOrganizationResponse> createOrganization(CreateOrganizationRequest request) {
    return Mono.fromRunnable(() -> logger.debug("createOrganization: enter, request: {}", request))
        .then(
            Mono.defer(
                () ->
                    CreateOrganization.builder()
                        .tokenVerifier(tokenVerifier)
                        .repository(repository)
                        .build()
                        .execute(request)))
        .doOnSuccess(
            response ->
                logger.debug(
                    "createOrganization: exit, response: {}, request: {}", response, request))
        .doOnError(th -> logger.error("createOrganization: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  @Override
  public Mono<GetMembershipResponse> getUserOrganizationsMembership(GetMembershipRequest request) {
    return Mono.fromRunnable(
        () -> logger.debug("getUserOrganizationsMembership: enter, request: {}", request))
        .then(
            Mono.defer(
                () ->
                    GetUserOrganizationsMembership.builder()
                        .tokenVerifier(tokenVerifier)
                        .repository(repository)
                        .build()
                        .execute(request)))
        .doOnSuccess(
            response ->
                logger.debug(
                    "getUserOrganizationsMembership: exit, request: {}, response: {} memberships",
                    request,
                    response.organizations().length))
        .doOnError(th -> logger.error("getUserOrganizationsMembership: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  @Override
  public Mono<DeleteOrganizationResponse> deleteOrganization(DeleteOrganizationRequest request) {
    return Mono.fromRunnable(() -> logger.debug("deleteOrganization: enter, request: {}", request))
        .then(
            Mono.defer(
                () ->
                    DeleteOrganization.builder()
                        .tokenVerifier(tokenVerifier)
                        .repository(repository)
                        .keyStore(keyStore)
                        .build()
                        .execute(request)))
        .doOnSuccess(
            response ->
                logger.debug(
                    "deleteOrganization: exit, response: {}, request: {}", response, request))
        .doOnError(th -> logger.error("deleteOrganization: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  @Override
  public Mono<UpdateOrganizationResponse> updateOrganization(UpdateOrganizationRequest request) {
    return Mono.fromRunnable(() -> logger.debug("updateOrganization: enter, request: {}", request))
        .then(
            Mono.defer(
                () ->
                    UpdateOrganization.builder()
                        .tokenVerifier(tokenVerifier)
                        .repository(repository)
                        .build()
                        .execute(request)))
        .doOnSuccess(
            response ->
                logger.debug(
                    "updateOrganization: exit, response: {}, request: {}", response, request))
        .doOnError(th -> logger.error("updateOrganization: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  @Override
  public Mono<GetOrganizationMembersResponse> getOrganizationMembers(
      GetOrganizationMembersRequest request) {
    return Mono.fromRunnable(
        () -> logger.debug("getOrganizationMembers: enter, request: {}", request))
        .then(
            Mono.defer(
                () ->
                    GetOrganizationMembers.builder()
                        .tokenVerifier(tokenVerifier)
                        .repository(repository)
                        .build()
                        .execute(request)))
        .doOnSuccess(
            response ->
                logger.debug(
                    "getOrganizationMembers: exit, response: {}, request: {}", response, request))
        .doOnError(th -> logger.error("getOrganizationMembers: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  @Override
  public Mono<InviteOrganizationMemberResponse> inviteMember(
      InviteOrganizationMemberRequest request) {
    return Mono.fromRunnable(() -> logger.debug("inviteMember: enter, request: {}", request))
        .then(
            Mono.defer(
                () ->
                    InviteMember.builder()
                        .tokenVerifier(tokenVerifier)
                        .repository(repository)
                        .build()
                        .execute(request)))
        .doOnSuccess(
            response ->
                logger.debug("inviteMember: exit, response: {}, request: {}", response, request))
        .doOnError(th -> logger.error("inviteMember: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  @Override
  public Mono<KickoutOrganizationMemberResponse> kickoutMember(
      KickoutOrganizationMemberRequest request) {
    return Mono.fromRunnable(() -> logger.debug("kickoutMember: enter, request: {}", request))
        .then(
            Mono.defer(
                () ->
                    KickoutMember.builder()
                        .tokenVerifier(tokenVerifier)
                        .repository(repository)
                        .build()
                        .execute(request)))
        .doOnSuccess(
            response ->
                logger.debug("kickoutMember: exit, response: {}, request: {}", response, request))
        .doOnError(th -> logger.error("kickoutMember: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  @Override
  public Mono<LeaveOrganizationResponse> leaveOrganization(LeaveOrganizationRequest request) {
    return Mono.fromRunnable(() -> logger.debug("leaveOrganization: enter, request: {}", request))
        .then(
            Mono.defer(
                () ->
                    LeaveOrganization.builder()
                        .tokenVerifier(tokenVerifier)
                        .repository(repository)
                        .build()
                        .execute(request)))
        .doOnSuccess(
            response ->
                logger.debug(
                    "leaveOrganization: exit, response: {}, request: {}", response, request))
        .doOnError(th -> logger.error("leaveOrganization: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  @Override
  public Mono<GetOrganizationResponse> addOrganizationApiKey(AddOrganizationApiKeyRequest request) {
    return Mono.fromRunnable(
        () -> logger.debug("addOrganizationApiKey: enter, request: {}", request))
        .then(
            Mono.defer(
                () ->
                    AddOrganizationApiKey.builder()
                        .tokenVerifier(tokenVerifier)
                        .repository(repository)
                        .keyPairGenerator(keyPairGenerator)
                        .keyStore(keyStore)
                        .build()
                        .execute(request)))
        .doOnSuccess(
            response ->
                logger.debug(
                    "addOrganizationApiKey: exit, response: {}, request: {}", response, request))
        .doOnError(th -> logger.error("addOrganizationApiKey: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  @Override
  public Mono<GetOrganizationResponse> deleteOrganizationApiKey(
      DeleteOrganizationApiKeyRequest request) {
    return Mono.fromRunnable(
        () -> logger.debug("deleteOrganizationApiKey: enter, request: {}", request))
        .then(
            Mono.defer(
                () ->
                    DeleteOrganizationApiKey.builder()
                        .tokenVerifier(tokenVerifier)
                        .repository(repository)
                        .keyStore(keyStore)
                        .build()
                        .execute(request)))
        .doOnSuccess(
            response ->
                logger.debug(
                    "deleteOrganizationApiKey: exit, response: {}, request: {}", response, request))
        .doOnError(th -> logger.error("deleteOrganizationApiKey: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  @Override
  public Mono<GetOrganizationResponse> getOrganization(GetOrganizationRequest request) {
    return Mono.fromRunnable(() -> logger.debug("getOrganization: enter, request: {}", request))
        .then(
            Mono.defer(
                () ->
                    GetOrganization.builder()
                        .tokenVerifier(tokenVerifier)
                        .repository(repository)
                        .build()
                        .execute(request)))
        .doOnSuccess(
            response ->
                logger.debug("getOrganization: exit, response: {}, request: {}", response, request))
        .doOnError(th -> logger.error("getOrganization: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  @Override
  public Mono<UpdateOrganizationMemberRoleResponse> updateOrganizationMemberRole(
      UpdateOrganizationMemberRoleRequest request) {
    return Mono.fromRunnable(
        () -> logger.debug("updateOrganizationMemberRole: enter, request: {}", request))
        .then(
            Mono.defer(
                () ->
                    UpdateOrganizationMemberRole.builder()
                        .tokenVerifier(tokenVerifier)
                        .repository(repository)
                        .build()
                        .execute(request)))
        .doOnSuccess(
            response ->
                logger.debug(
                    "updateOrganizationMemberRole: exit, response: {}, request: {}",
                    response,
                    request))
        .doOnError(th -> logger.error("updateOrganizationMemberRole: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  @Override
  public Mono<GetPublicKeyResponse> getPublicKey(GetPublicKeyRequest request) {
    return Mono.fromRunnable(() -> logger.debug("getPublicKey: enter, request: {}", request))
        .then(Mono.fromCallable(() -> keyStore.getPublicKey(request.keyId())))
        .map(
            publicKey ->
                new GetPublicKeyResponse(
                    publicKey.getAlgorithm(),
                    publicKey.getFormat(),
                    publicKey.getEncoded(),
                    request.keyId()))
        .doOnSuccess(
            response ->
                logger.debug("getPublicKey: exit: response: {}, request: {}", response, request))
        .doOnError(th -> logger.error("getPublicKey: ERROR", th))
        .onErrorMap(ServiceOperationException.class, Throwable::getCause);
  }

  private KeyPairGenerator keyPairGenerator() {
    try {
      String algorithm = AppConfiguration.configRegistry().stringValue("crypto.algorithm", "RSA");
      int keySize = AppConfiguration.configRegistry().intValue("crypto.key.size", 2048);

      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
      keyPairGenerator.initialize(keySize);

      return keyPairGenerator;
    } catch (NoSuchAlgorithmException e) {
      throw new OrganizationServiceException("Error during initialing KeyPairGenerator", e);
    }
  }
}
