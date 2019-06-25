package io.scalecube.organization.operation;

import io.scalecube.account.api.OrganizationInfo;
import io.scalecube.organization.repository.OrganizationsRepository;
import io.scalecube.organization.repository.exception.InvalidInputException;
import io.scalecube.organization.repository.exception.NameAlreadyInUseException;
import io.scalecube.organization.tokens.TokenVerifier;
import reactor.core.publisher.Mono;

public abstract class OrganizationInfoOperation<I, O> extends ServiceOperation<I, O> {
  private static final String VALID_ORG_NAME_CHARS_REGEX = "^[.%a-zA-Z0-9_-]*$";

  OrganizationInfoOperation(TokenVerifier tokenVerifier, OrganizationsRepository repository) {
    super(tokenVerifier, repository);
  }

  protected Mono<Void> validate(OrganizationInfo info, OperationServiceContext context) {
    return Mono.fromRunnable(
        () -> {
          requireNonNullOrEmpty(info.id(), "organizationId is a required argument");
          requireNonNullOrEmpty(info.email(), "Organization email cannot be empty");
          requireNonNullOrEmpty(info.name(), "Organization name cannot be empty");
          boolean invalidOrgName = !info.name().matches(VALID_ORG_NAME_CHARS_REGEX);
          if (invalidOrgName) {
            throw new InvalidInputException(
                "Organization name can only contain characters in range A-Z, a-z, 0-9 "
                    + "as well as underscore, period, dash & percent");
          }
        })
        .then(context.repository().existsByName(info.name()))
        .doOnNext(
            exists -> {
              if (exists) {
                throw new NameAlreadyInUseException(
                    String.format("Organization name: '%s' already in use", info.name()));
              }
            })
        .then();
  }
}
